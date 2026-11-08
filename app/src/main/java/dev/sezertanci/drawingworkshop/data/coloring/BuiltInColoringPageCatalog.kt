package dev.sezertanci.drawingworkshop.data.coloring

import dev.sezertanci.drawingworkshop.R
import javax.inject.Inject

/** Uygulamayla gelen boyama sayfaları. Kimlikler kayıtlarda saklandığı için değiştirilmez. */
class BuiltInColoringPageCatalog @Inject constructor() : ColoringPageCatalog {

    override val pages: List<ColoringPage> = listOf(
        ColoringPage("cat", R.drawable.coloring_cat),
        ColoringPage("fish", R.drawable.coloring_fish),
        ColoringPage("house", R.drawable.coloring_house),
        ColoringPage("car", R.drawable.coloring_car),
        ColoringPage("flower", R.drawable.coloring_flower),
        ColoringPage("butterfly", R.drawable.coloring_butterfly),
        ColoringPage("dinosaur", R.drawable.coloring_dinosaur),
        ColoringPage("sun", R.drawable.coloring_sun),
        ColoringPage("carriage", R.drawable.coloring_carriage),
    )

    private val byId = pages.associateBy(ColoringPage::id)

    override fun find(id: String): ColoringPage? = byId[id]
}
