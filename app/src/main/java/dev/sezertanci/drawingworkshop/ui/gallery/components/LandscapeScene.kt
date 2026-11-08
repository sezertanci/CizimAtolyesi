package dev.sezertanci.drawingworkshop.ui.gallery.components

import androidx.annotation.DrawableRes
import dev.sezertanci.drawingworkshop.R
import kotlin.random.Random

/**
 * Sahnedeki tek bir nesne. Konum ve genişlik ekran boyutuna oranla verilir:
 * [left] sol kenar, [bottom] alt kenar, [width] genişlik (0 = sol/üst kenar, 1 = sağ/alt kenar).
 * [heightScale] resmi dikeyde basıklaştırır (< 1) veya uzatır (> 1).
 */
data class SceneItem(
    @DrawableRes val image: Int,
    val left: Float,
    val bottom: Float,
    val width: Float,
    val heightScale: Float = 1f,
)

/** Bir nesnenin rastgele konumlanabileceği bölge. */
private data class Zone(
    val left: ClosedFloatingPointRange<Float>,
    val bottom: ClosedFloatingPointRange<Float>,
    val width: ClosedFloatingPointRange<Float>,
    val heightScale: Float = 1f,
)

// Yere basan nesnelerin (dağ, gökkuşağı) tabanı ekranın alt kenarındadır; böylece nereye
// yerleşirlerse yerleşsinler havada kalmazlar ve tabanları tepelerin arkasında gizlenir.
private val GroundLevel = 1.0f..1.02f

private val SunZone = Zone(left = 0.03f..0.87f, bottom = 0.18f..0.26f, width = 0.09f..0.11f)
private val RainbowZone = Zone(left = 0.05f..0.55f, bottom = GroundLevel, width = 0.40f..0.48f)
private val MountainZone = Zone(left = -0.08f..0.70f, bottom = GroundLevel, width = 0.40f..0.50f, heightScale = 1.2f)
private val CloudZone = Zone(left = -0.05f..0.92f, bottom = 0.15f..0.40f, width = 0.09f..0.14f)
private val TreeZone = Zone(left = 0.05f..0.85f, bottom = 0.95f..0.99f, width = 0.09f..0.11f)

// Tepeler ekranın altını boşluk bırakmadan kaplamalı; bu yüzden sıralı ve üst üste binerek dizilir.
private const val HILL_COUNT = 3
private const val HILL_FIRST_LEFT = -0.15f
private const val HILL_STEP = 0.4f
private const val HILL_JITTER = 0.05f
private const val HILL_HEIGHT_SCALE = 0.6f
private val HillBottom = 1.03f..1.06f
private val HillWidth = 0.55f..0.65f

private val MountainCount = 1..2
private val CloudCount = 2..4
private const val TREE_CHANCE = 0.7f

/**
 * Galeri arka planı için rastgele bir manzara düzeni üretir. Nesneler arkadan öne doğru sıralıdır.
 * Her nesnenin kendi bölgesi olduğu için mantıksız sahneler (yerde güneş, havada dağ) oluşmaz.
 */
fun randomLandscape(random: Random): List<SceneItem> = buildList {
    add(random.place(R.drawable.scene_sun, SunZone))
    add(random.place(R.drawable.scene_rainbow, RainbowZone))
    repeat(random.count(MountainCount)) { add(random.place(R.drawable.scene_mountain, MountainZone)) }
    repeat(random.count(CloudCount)) { add(random.place(R.drawable.scene_cloud, CloudZone)) }
    repeat(HILL_COUNT) { index -> add(random.hill(index)) }
    if (random.nextFloat() < TREE_CHANCE) add(random.place(R.drawable.scene_tree, TreeZone))
}

private fun Random.place(@DrawableRes image: Int, zone: Zone) =
    SceneItem(image, between(zone.left), between(zone.bottom), between(zone.width), zone.heightScale)

private fun Random.hill(index: Int) = SceneItem(
    image = R.drawable.scene_hill,
    left = HILL_FIRST_LEFT + index * HILL_STEP + between(-HILL_JITTER..HILL_JITTER),
    bottom = between(HillBottom),
    width = between(HillWidth),
    heightScale = HILL_HEIGHT_SCALE,
)

private fun Random.between(range: ClosedFloatingPointRange<Float>): Float =
    range.start + nextFloat() * (range.endInclusive - range.start)

private fun Random.count(range: IntRange): Int = nextInt(range.first, range.last + 1)
