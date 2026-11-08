package dev.sezertanci.drawingworkshop.ui.navigation

/** Ekranların gezinme yolları. */
object Routes {
    const val GALLERY = "gallery"
    const val TRASH = "trash"
    const val ARG_DRAWING_ID = "drawingId"
    private const val DRAWING = "drawing"
    const val DRAWING_PATTERN = "$DRAWING?$ARG_DRAWING_ID={$ARG_DRAWING_ID}"

    /** Kimlik verilmezse yeni bir resim açılır. */
    fun drawing(id: String?): String = if (id == null) DRAWING else "$DRAWING?$ARG_DRAWING_ID=$id"
}
