"""Boyama sayfalarının SVG kaynaklarını Android VectorDrawable dosyalarına çevirir.

Kullanım:
    pip install -r tools/coloring/requirements.txt
    python tools/coloring/build.py            # tüm sayfalar
    python tools/coloring/build.py cat fish   # yalnızca verilenler

Kaynaklar `pages/<id>.svg` dosyalarıdır ve bir illüstrasyon gibi arkadan öne çizilir:
- Beyaz dolgulu şekil (fill="#fff") öndeki nesnedir; arkasında kalan çizgiler kırpılır, kendisi
  çizilmez. Böylece üst üste binen şekillerde çizgiler birbirinin içine taşmaz ve bölgeler kapalı kalır.
- Siyah dolgulu şekil (göz bebeği gibi) dolu çizilir; `fill-rule="evenodd"` ile içinde boşluk bırakılabilir.
- Dolgusuz şekiller yalnızca çizgidir.
- `data-tube="40"` verilen yol, o kalınlıkta uçları yuvarlak bir tüpe dönüşür (kuyruk, sap, bacak).
  Tüp beyaz dolgulu bir şekil gibi davranır.
- `data-clip="<id>"` verilen çizgiler yalnızca o kimlikli şeklin içinde kalır (pul, çizgi, desen).
Çıktı `app/src/main/res/drawable/coloring_<id>.xml`; önizleme için düzleştirilmiş SVG `out/<id>.svg`.
"""

from __future__ import annotations

import sys
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path

from shapely.geometry import LineString, MultiLineString, Polygon
from shapely.geometry.base import BaseGeometry
from shapely.ops import linemerge, unary_union
from svgelements import SVG, Close, Line, Move, Path as SvgPath, Shape

TOOL_DIR = Path(__file__).resolve().parent
ROOT = TOOL_DIR.parents[1]
PAGES_DIR = TOOL_DIR / "pages"
PREVIEW_DIR = TOOL_DIR / "out"
OUT_DIR = ROOT / "app" / "src" / "main" / "res" / "drawable"
VIEWPORT = (1400, 1000)
LINE_COLOR = "@color/line_art"
SAMPLE_STEP = 2.0
SIMPLIFY_TOLERANCE = 0.15
INDENT = "    "


@dataclass
class Item:
    rings: list[list[tuple[float, float]]]
    closed: list[bool]
    stroke_width: float | None
    fill: str | None  # None, "white" (örtücü) veya "black" (dolu)
    even_odd: bool
    id: str | None = None
    clip: str | None = None

    def area(self) -> BaseGeometry:
        shape: BaseGeometry = Polygon()
        for ring, closed in zip(self.rings, self.closed):
            if closed and len(ring) >= 3:
                shape = shape.symmetric_difference(Polygon(ring).buffer(0))
        return shape


def sample(segment) -> list[tuple[float, float]]:
    if isinstance(segment, (Line, Close)):
        return [tuple(segment.end)]
    length = segment.length(error=1e-3)
    count = max(4, int(length / SAMPLE_STEP))
    return [tuple(segment.point(i / count)) for i in range(1, count + 1)]


def to_item(shape: Shape) -> Item | None:
    path = abs(SvgPath(shape))
    rings, closed = [], []
    for sub in path.as_subpaths():
        points: list[tuple[float, float]] = []
        is_closed = False
        for seg in sub:
            if isinstance(seg, Move):
                points = [tuple(seg.end)]
                continue
            points += sample(seg)
            is_closed = is_closed or isinstance(seg, Close)
        if len(points) >= 2:
            rings.append(points)
            closed.append(is_closed)
    stroke = shape.stroke is not None and shape.stroke.value is not None
    tube = shape.values.get("data-tube")
    if tube:
        rings, closed = tube_rings(rings, float(tube))
    fill = None
    if shape.fill is not None and shape.fill.value is not None:
        fill = "black" if shape.fill.red < 128 else "white"
    width = round(shape.implicit_stroke_width, 1) if stroke else None
    even_odd = str(shape.values.get("fill-rule", "")).lower() == "evenodd"
    if tube:
        fill = fill or "white"
    item = Item(rings, closed, width, fill, even_odd, shape.id, shape.values.get("data-clip"))
    return item if rings else None


def tube_rings(lines: list[list[tuple[float, float]]], width: float):
    """Her alt yolu tüpe çevirip birleştirir; dallı şekiller (mercan, ağaç) tek parça olur."""
    outline = unary_union([LineString(points).buffer(width / 2, quad_segs=24) for points in lines])
    polygons = outline.geoms if hasattr(outline, "geoms") else [outline]
    rings = [list(r.coords) for p in polygons for r in [p.exterior, *p.interiors]]
    return rings, [True] * len(rings)


def fmt(value: float) -> str:
    text = f"{value:.1f}".rstrip("0").rstrip(".")
    return "0" if text == "-0" else text


def line_data(geometry: BaseGeometry) -> list[str]:
    if geometry.is_empty:
        return []
    lines = geometry.geoms if hasattr(geometry, "geoms") else [geometry]
    merged = linemerge([g for g in lines if isinstance(g, LineString)]) if len(lines) > 1 else lines[0]
    parts = merged.geoms if hasattr(merged, "geoms") else [merged]
    result = []
    for part in parts:
        coords = list(part.simplify(SIMPLIFY_TOLERANCE).coords)
        if len(coords) < 2:
            continue
        ring = coords[0] == coords[-1] and len(coords) > 3
        result.append(points_data(coords[:-1] if ring else coords, ring))
    return result


def points_data(coords: list[tuple[float, float]], closed: bool) -> str:
    first, *rest = (f"{fmt(x)},{fmt(y)}" for x, y in coords)
    return f"M{first} L{' '.join(rest)}{' Z' if closed else ''}"


def polygon_data(geometry: BaseGeometry) -> list[str]:
    polygons = geometry.geoms if hasattr(geometry, "geoms") else [geometry]
    result = []
    for polygon in polygons:
        if polygon.is_empty or not isinstance(polygon, Polygon):
            continue
        for ring in [polygon.exterior, *polygon.interiors]:
            result.append(points_data(list(ring.simplify(SIMPLIFY_TOLERANCE).coords)[:-1], True))
    return result


def clip_targets(items: list[Item]) -> dict[int, BaseGeometry]:
    """Her kırpılan çizgiyi, kendinden önce gelen aynı kimlikli şekle bağlar (aynalı kopyalar için)."""
    areas: dict[str, BaseGeometry] = {}
    targets: dict[int, BaseGeometry] = {}
    for index, item in enumerate(items):
        if item.id:
            areas[item.id] = item.area()
        if item.clip:
            targets[index] = areas[item.clip]
    return targets


def flatten(items: list[Item]) -> tuple[dict[float, list[str]], list[str]]:
    """Her öğenin önündeki beyaz/siyah şekillerin örttüğü kısımlarını atar."""
    strokes: dict[float, list[str]] = defaultdict(list)
    fills: list[str] = []
    front: BaseGeometry = Polygon()
    clip_areas = clip_targets(items)
    for index, item in reversed(list(enumerate(items))):
        if item.stroke_width:
            lines = MultiLineString([r + [r[0]] if c else r for r, c in zip(item.rings, item.closed)])
            if item.clip:
                lines = lines.intersection(clip_areas[index].buffer(1))
            visible = lines.difference(front) if not front.is_empty else lines
            strokes[item.stroke_width] += line_data(visible)
        if item.fill:
            area = item.area()
            if item.fill == "black":
                fills += polygon_data(area.difference(front) if not front.is_empty else area)
            front = unary_union([front, area])
    return strokes, fills


def vector_xml(name: str, strokes: dict[float, list[str]], fills: list[str]) -> str:
    paths = []
    for width, data in sorted(strokes.items()):
        if data:
            paths.append(
                f'{INDENT}<path\n{INDENT * 2}android:pathData="{" ".join(data)}"\n'
                f'{INDENT * 2}android:strokeWidth="{fmt(width)}"\n'
                f'{INDENT * 2}android:strokeColor="{LINE_COLOR}"\n'
                f'{INDENT * 2}android:strokeLineCap="round"\n'
                f'{INDENT * 2}android:strokeLineJoin="round" />'
            )
    if fills:
        paths.append(
            f'{INDENT}<path\n{INDENT * 2}android:pathData="{" ".join(fills)}"\n'
            f'{INDENT * 2}android:fillColor="{LINE_COLOR}"\n'
            f'{INDENT * 2}android:fillType="evenOdd" />'
        )
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        f"<!-- tools/coloring/build.py ile üretilir; kaynak: tools/coloring/pages/{name}.svg -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'{INDENT}android:width="700dp"\n{INDENT}android:height="500dp"\n'
        f'{INDENT}android:viewportWidth="{VIEWPORT[0]}"\n{INDENT}android:viewportHeight="{VIEWPORT[1]}">\n'
        + "\n".join(paths)
        + "\n</vector>\n"
    )


def preview_svg(strokes: dict[float, list[str]], fills: list[str]) -> str:
    body = [
        f'<path d="{" ".join(d)}" fill="none" stroke="#000" stroke-width="{fmt(w)}" '
        'stroke-linecap="round" stroke-linejoin="round"/>'
        for w, d in strokes.items() if d
    ]
    if fills:
        body.append(f'<path d="{" ".join(fills)}" fill="#000" fill-rule="evenodd"/>')
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {VIEWPORT[0]} {VIEWPORT[1]}" '
        f'width="{VIEWPORT[0]}" height="{VIEWPORT[1]}">\n' + "\n".join(body) + "\n</svg>\n"
    )


def build(source: Path) -> Path:
    svg = SVG.parse(str(source), reify=True)
    items = [item for el in svg.elements() if isinstance(el, Shape) and (item := to_item(el))]
    strokes, fills = flatten(items)
    target = OUT_DIR / f"coloring_{source.stem}.xml"
    target.write_text(vector_xml(source.stem, strokes, fills), encoding="utf-8")
    PREVIEW_DIR.mkdir(exist_ok=True)
    (PREVIEW_DIR / source.name).write_text(preview_svg(strokes, fills), encoding="utf-8")
    return target


def main(names: list[str]) -> int:
    for source in sorted(PAGES_DIR.glob("*.svg")):
        if not names or source.stem in names:
            target = build(source)
            print(f"{target.relative_to(ROOT)}  ({target.stat().st_size // 1024} KB)")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
