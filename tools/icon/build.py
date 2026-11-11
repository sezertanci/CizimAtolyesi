"""Uygulama simgesini tek kaynaktan üretir.

Kullanım:
    python tools/icon/build.py

Çıktılar:
- `app/src/main/res/drawable/ic_launcher_foreground.xml`: uyarlanabilir simgenin ön planı.
- `store/drawing-workshop/graphics/icon.svg`: mağaza simgesi (zeminle birlikte, görünen 72 birimlik orta alan).

Mağaza PNG'si SVG'den Chrome ile alınır (Windows'ta tam yol gerekebilir):
    chrome --headless --screenshot=store/drawing-workshop/graphics/icon-512.png \
        --window-size=512,512 store/drawing-workshop/graphics/icon.svg

Simge: gökkuşağı renkli ipek fırça çiçeği, işi bitip duvara yaslanmış ve ucundan boya akan fırça,
köşede yıldız çıkartması. Zemin rengi `values/colors.xml` içindeki `launcher_background` ile aynı tutulur.
Gradyanlar her şeklin kendi koordinat sisteminde tanımlıdır; bu yüzden döndürülen her ilmekle birlikte
dönerler ve ipek fırçanın renk geçişi elde edilir (SVG'de `userSpaceOnUse`, VectorDrawable'da varsayılan).
"""

from __future__ import annotations

import itertools
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FOREGROUND_XML = ROOT.joinpath("app", "src", "main", "res", "drawable", "ic_launcher_foreground.xml")
STORE_SVG = ROOT.joinpath("store", "drawing-workshop", "graphics", "icon.svg")

VIEWPORT = 108
CENTER = VIEWPORT / 2
VISIBLE = 72  # maskenin gösterdiği orta alan
SAFE_SCALE = 0.8  # tüm çizim güvenli alana sığsın diye merkeze göre küçültülür

BACKGROUND = "#2E1065"
RAINBOW = ("#FF00D4", "#304FFE", "#00E5FF", "#00E676", "#FFEA00", "#FF3D00")
BRISTLE_COLORS = ("#FF00D4", "#304FFE", "#00E5FF", "#FFEA00", "#5D4037", "#5D4037")
PAINT = "#FF00D4"
SHADOW = "#000000"
SHADOW_ALPHA = 0.25
WHITE = "#FFFFFF"

STAR = "M0,-10 L2.9,-4 L9.5,-3.1 L4.7,1.5 L5.9,8.1 L0,5 L-5.9,8.1 L-4.7,1.5 L-9.5,-3.1 L-2.9,-4 Z"
BRUSH_HANDLE = "M-2.6,5 L2.6,5 L1.8,26 Q0,28 -1.8,26 Z"
BENT_BRISTLES = "M-3.4,-1 C-4.2,-7 -2.5,-12 2,-14.8 C4,-16 6.8,-15.6 6.2,-13.6 C5.2,-10.5 3.6,-7 3.4,-1 Z"


@dataclass(frozen=True)
class Gradient:
    """x=0 ekseni boyunca yukarıdan aşağı doğrusal renk geçişi."""

    y1: float
    y2: float
    colors: tuple[str, ...]


Paint = str | Gradient | None


@dataclass(frozen=True)
class Shape:
    d: str
    fill: Paint = None
    stroke: Paint = None
    width: float = 0
    alpha: float = 1
    round_join: bool = False


@dataclass(frozen=True)
class Group:
    """VectorDrawable sırası: pivota göre ölçekle, döndür, sonra taşı."""

    children: tuple[Shape | Group, ...]
    tx: float = 0
    ty: float = 0
    rotation: float = 0
    scale: float = 1
    pivot: tuple[float, float] = (0, 0)


def ellipse(cx: float, cy: float, rx: float, ry: float) -> str:
    return f"M{cx - rx},{cy} a{rx},{ry} 0 1,0 {2 * rx},0 a{rx},{ry} 0 1,0 {-2 * rx},0"


def rect(x: float, y: float, w: float, h: float) -> str:
    return f"M{x},{y} h{w} v{h} h{-w} Z"


# --- Simgenin parçaları ---


def silk_flower() -> Group:
    outer = Gradient(-28, -2, RAINBOW)
    inner = Gradient(-15, -1, RAINBOW[::-1])
    loops = [
        Group((Shape(ellipse(0, -15, 7, 13), stroke=outer, width=4.2),), rotation=k * 45) for k in range(8)
    ] + [
        Group((Shape(ellipse(0, -8, 4, 7), stroke=inner, width=3.4),), rotation=22.5 + k * 45) for k in range(8)
    ]
    center = Shape(ellipse(0, 0, 3, 3), fill="#FFEA00")
    return Group((*loops, center), tx=46, ty=47, scale=0.88)


def leaning_brush() -> Group:
    """Sapı yukarıda, kılları yere basmaktan bükülmüş fırça."""
    parts = (
        Group((Shape(BRUSH_HANDLE, fill=SHADOW, alpha=SHADOW_ALPHA),), tx=-1, ty=-1),
        Shape(BRUSH_HANDLE, fill="#FF7043"),
        Shape("M-0.6,7 L0.4,7 L0.2,24 L-0.4,24 Z", fill="#FFAB91"),
        Shape(rect(-3.6, -2, 7.2, 7.5), fill="#CFD8DC"),
        Shape(rect(-3.6, 1.2, 7.2, 1.4), fill="#90A4AE"),
        Shape(BENT_BRISTLES, fill=Gradient(-15.5, -1, BRISTLE_COLORS)),
    )
    return Group(parts, tx=75, ty=62, rotation=195, scale=1.2)


def paint_drip() -> Group:
    """Fırçanın ucundan yere akan boya ve birikintisi."""
    return Group((
        Shape(ellipse(65.5, 84, 7, 2), fill=PAINT),
        Shape("M63.4,77.1 L65.6,77.1 L65.2,79 Q64.5,80.5 63.8,79 Z", fill=PAINT),
        Shape("M64.5,77.5 C66.1,80 66.5,82.5 64.5,82.8 C62.5,82.5 62.9,80 64.5,77.5 Z", fill=PAINT),
    ))


def star_sticker() -> Group:
    shadow = Shape(STAR, fill=SHADOW, stroke=SHADOW, width=5, alpha=SHADOW_ALPHA, round_join=True)
    return Group((
        Group((shadow,), tx=0.8, ty=1.2),
        Shape(STAR, stroke=WHITE, width=4.5, round_join=True),  # beyaz çıkartma kenarı dolgunun altında kalır
        Shape(STAR, fill="#FFCA28"),
    ), tx=31, ty=74, scale=0.75)


def icon() -> Group:
    return Group((silk_flower(), leaning_brush(), paint_drip(), star_sticker()),
                 scale=SAFE_SCALE, pivot=(CENTER, CENTER))


# --- SVG çıktısı ---


def svg_transform(g: Group) -> str:
    px, py = g.pivot
    return (f"translate({g.tx + px},{g.ty + py}) rotate({g.rotation}) "
            f"scale({g.scale}) translate({-px},{-py})")


def svg_gradient(gid: str, g: Gradient) -> str:
    stops = "".join(f'<stop offset="{i / (len(g.colors) - 1):.2f}" stop-color="{c}"/>'
                    for i, c in enumerate(g.colors))
    return (f'<linearGradient id="{gid}" gradientUnits="userSpaceOnUse" '
            f'x1="0" y1="{g.y1}" x2="0" y2="{g.y2}">{stops}</linearGradient>')


def svg_paint(p: Paint, defs: list[str], ids: itertools.count) -> str:
    if p is None:
        return "none"
    if isinstance(p, str):
        return p
    gid = f"g{next(ids)}"
    defs.append(svg_gradient(gid, p))
    return f"url(#{gid})"


def svg_node(n: Shape | Group, defs: list[str], ids: itertools.count) -> str:
    if isinstance(n, Group):
        inner = "".join(svg_node(c, defs, ids) for c in n.children)
        return f'<g transform="{svg_transform(n)}">{inner}</g>'
    attrs = f'd="{n.d}" fill="{svg_paint(n.fill, defs, ids)}"'
    if n.stroke is not None:
        attrs += f' stroke="{svg_paint(n.stroke, defs, ids)}" stroke-width="{n.width}"'
    if n.round_join:
        attrs += ' stroke-linejoin="round"'
    if n.alpha != 1:
        attrs += f' opacity="{n.alpha}"'
    return f"<path {attrs}/>"


def store_svg() -> str:
    defs: list[str] = []
    body = svg_node(icon(), defs, itertools.count())
    m = (VIEWPORT - VISIBLE) / 2
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{m} {m} {VISIBLE} {VISIBLE}" width="512" height="512">\n'
            "  <!-- tools/icon/build.py ile üretilir; elle düzenlemeyin. Play kenarları kendisi yuvarlar. -->\n"
            f'  <defs>{"".join(defs)}</defs>\n'
            f'  <rect width="{VIEWPORT}" height="{VIEWPORT}" fill="{BACKGROUND}"/>\n'
            f"  {body}\n</svg>\n")


# --- VectorDrawable çıktısı ---

INDENT = "    "


def argb(color: str) -> str:
    return "#FF" + color.lstrip("#").upper()


def vd_group_attrs(g: Group) -> list[str]:
    px, py = g.pivot
    values = {"pivotX": px, "pivotY": py, "rotation": g.rotation, "scaleX": g.scale, "scaleY": g.scale,
              "translateX": g.tx, "translateY": g.ty}
    defaults = {"scaleX": 1, "scaleY": 1}
    return [f'android:{k}="{v:g}"' for k, v in values.items() if v != defaults.get(k, 0)]


def vd_gradient(attr: str, g: Gradient, pad: str) -> list[str]:
    lines = [f'{pad}<aapt:attr name="android:{attr}">',
             f'{pad}{INDENT}<gradient android:type="linear" android:startX="0" android:startY="{g.y1:g}"'
             f' android:endX="0" android:endY="{g.y2:g}">']
    lines += [f'{pad}{INDENT * 2}<item android:offset="{i / (len(g.colors) - 1):.2f}" android:color="{argb(c)}" />'
              for i, c in enumerate(g.colors)]
    return lines + [f"{pad}{INDENT}</gradient>", f"{pad}</aapt:attr>"]


def vd_path(s: Shape, pad: str) -> list[str]:
    attrs = [f'android:pathData="{s.d}"']
    gradients: list[str] = []
    for attr, paint in (("fillColor", s.fill), ("strokeColor", s.stroke)):
        if isinstance(paint, str):
            attrs.append(f'android:{attr}="{argb(paint)}"')
        elif isinstance(paint, Gradient):
            gradients += vd_gradient(attr, paint, pad + INDENT)
    if s.stroke is not None:
        attrs.append(f'android:strokeWidth="{s.width:g}"')
    if s.round_join:
        attrs.append('android:strokeLineJoin="round"')
    if s.alpha != 1:
        attrs += [f'android:fillAlpha="{s.alpha:g}"', f'android:strokeAlpha="{s.alpha:g}"']
    head = f"{pad}<path " + f"\n{pad}{INDENT}".join(attrs)
    if not gradients:
        return [head + " />"]
    return [head + ">", *gradients, f"{pad}</path>"]


def vd_node(n: Shape | Group, pad: str) -> list[str]:
    if isinstance(n, Shape):
        return vd_path(n, pad)
    attrs = vd_group_attrs(n)
    open_tag = f"{pad}<group" + "".join(f" {a}" for a in attrs) + ">"
    inner = [line for c in n.children for line in vd_node(c, pad + INDENT)]
    return [open_tag, *inner, f"{pad}</group>"]


def foreground_xml() -> str:
    header = [
        '<?xml version="1.0" encoding="utf-8"?>',
        "<!-- tools/icon/build.py ile üretilir; elle düzenlemeyin. -->",
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'{INDENT}xmlns:aapt="http://schemas.android.com/aapt"',
        f'{INDENT}android:width="{VIEWPORT}dp"',
        f'{INDENT}android:height="{VIEWPORT}dp"',
        f'{INDENT}android:viewportWidth="{VIEWPORT}"',
        f'{INDENT}android:viewportHeight="{VIEWPORT}">',
    ]
    return "\n".join(header + vd_node(icon(), INDENT) + ["</vector>"]) + "\n"


def main() -> None:
    FOREGROUND_XML.write_text(foreground_xml(), encoding="utf-8", newline="\n")
    STORE_SVG.write_text(store_svg(), encoding="utf-8", newline="\n")
    print(f"Yazıldı: {FOREGROUND_XML.relative_to(ROOT)}, {STORE_SVG.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
