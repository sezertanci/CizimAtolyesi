package dev.sezertanci.drawingworkshop.data.coloring

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Size
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sezertanci.drawingworkshop.domain.geometry.fitCenter
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Sayfaları vektör çizimden çizer; çizgiler her boyutta keskin kalır. Her çağrıda çizimin ayrı bir
 * kopyası kullanılır, bu yüzden arka plandaki iş parçacıklarından güvenle çağrılabilir.
 */
class DrawableColoringPageRenderer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val catalog: ColoringPageCatalog,
) : ColoringPageRenderer {

    override fun draw(templateId: String, canvas: Canvas, width: Int, height: Int) {
        val drawable = drawable(templateId) ?: return
        val bounds = fitCenter(drawable.intrinsicWidth, drawable.intrinsicHeight, width, height)
        drawable.setBounds(
            bounds.left.roundToInt(),
            bounds.top.roundToInt(),
            (bounds.left + bounds.width).roundToInt(),
            (bounds.top + bounds.height).roundToInt(),
        )
        drawable.draw(canvas)
    }

    override fun artSize(templateId: String): Size? =
        drawable(templateId)?.let { Size(it.intrinsicWidth, it.intrinsicHeight) }

    private fun drawable(templateId: String): Drawable? {
        val page = catalog.find(templateId) ?: return null
        return context.getDrawable(page.art)?.mutate()
    }
}
