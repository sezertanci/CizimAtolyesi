package dev.sezertanci.drawingworkshop.domain.symmetry

import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryTransform.Companion.IDENTITY
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryTransform.Companion.MIRROR_ANTI_DIAGONAL
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryTransform.Companion.MIRROR_DIAGONAL
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryTransform.Companion.MIRROR_X
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryTransform.Companion.MIRROR_Y
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryTransform.Companion.ROTATE_180
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryTransform.Companion.ROTATE_270
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryTransform.Companion.ROTATE_90

/** Ekranda kılavuz olarak çizilen ayna ekseninin yönü. */
enum class SymmetryAxis(val dx: Float, val dy: Float) {
    VERTICAL(0f, 1f),
    HORIZONTAL(1f, 0f),
    DIAGONAL(1f, 1f),
    ANTI_DIAGONAL(1f, -1f),
}

/**
 * Simetri modları. Her mod, çizginin kaç kopya çizileceğini [transforms] ile belirler; ilk dönüşüm her
 * zaman çizginin kendisidir. 4 ve 8 parça kaleydoskop gibi çalışır: kopyalar hem döner hem aynalanır.
 */
enum class SymmetryMode(val transforms: List<SymmetryTransform>, val axes: List<SymmetryAxis>) {
    OFF(listOf(IDENTITY), emptyList()),
    LEFT_RIGHT(listOf(IDENTITY, MIRROR_X), listOf(SymmetryAxis.VERTICAL)),
    TOP_BOTTOM(listOf(IDENTITY, MIRROR_Y), listOf(SymmetryAxis.HORIZONTAL)),
    FOUR(
        listOf(IDENTITY, MIRROR_X, MIRROR_Y, ROTATE_180),
        listOf(SymmetryAxis.VERTICAL, SymmetryAxis.HORIZONTAL),
    ),
    EIGHT(
        listOf(IDENTITY, ROTATE_90, ROTATE_180, ROTATE_270, MIRROR_X, MIRROR_Y, MIRROR_DIAGONAL, MIRROR_ANTI_DIAGONAL),
        SymmetryAxis.entries,
    ),
}
