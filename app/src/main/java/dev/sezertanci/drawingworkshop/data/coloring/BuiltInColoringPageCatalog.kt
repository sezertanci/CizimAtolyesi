package dev.sezertanci.drawingworkshop.data.coloring

import dev.sezertanci.drawingworkshop.R
import javax.inject.Inject

/** Uygulamayla gelen boyama sayfaları. Kimlikler kayıtlarda saklandığı için değiştirilmez. */
class BuiltInColoringPageCatalog @Inject constructor() : ColoringPageCatalog {

    /** Eklenme sırası: yeni sayfa listenin sonuna yazılır. */
    private val pagesInAddedOrder = listOf(
        ColoringPage("cat", R.drawable.coloring_cat),
        ColoringPage("fish", R.drawable.coloring_fish),
        ColoringPage("house", R.drawable.coloring_house),
        ColoringPage("car", R.drawable.coloring_car),
        ColoringPage("flower", R.drawable.coloring_flower),
        ColoringPage("butterfly", R.drawable.coloring_butterfly),
        ColoringPage("dinosaur", R.drawable.coloring_dinosaur),
        ColoringPage("sun", R.drawable.coloring_sun),
        ColoringPage("carriage", R.drawable.coloring_carriage),
        ColoringPage("owl", R.drawable.coloring_owl),
        ColoringPage("turtle", R.drawable.coloring_turtle),
        ColoringPage("rocket", R.drawable.coloring_rocket),
        ColoringPage("elephant", R.drawable.coloring_elephant),
        ColoringPage("sailboat", R.drawable.coloring_sailboat),
        ColoringPage("bunny", R.drawable.coloring_bunny),
        ColoringPage("fairy", R.drawable.coloring_fairy),
        ColoringPage("pony", R.drawable.coloring_pony),
        ColoringPage("cyclist", R.drawable.coloring_cyclist),
        ColoringPage("princess", R.drawable.coloring_princess),
        ColoringPage("ballgown", R.drawable.coloring_ballgown),
    )

    /** Yeniden eskiye sıralı; yeni eklenen sayfalar galeride ilk sırada görünür. */
    override val pages: List<ColoringPage> = pagesInAddedOrder.asReversed()

    private val byId = pages.associateBy(ColoringPage::id)

    override fun find(id: String): ColoringPage? = byId[id]
}
