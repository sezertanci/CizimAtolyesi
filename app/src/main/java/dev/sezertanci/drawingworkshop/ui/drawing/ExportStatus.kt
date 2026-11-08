package dev.sezertanci.drawingworkshop.ui.drawing

/** Galeriye aktarma düğmesinin durumu; çocuklar sonucu düğmenin simgesinden anlar. */
enum class ExportStatus {
    IDLE,
    RUNNING,
    DONE,
    FAILED,
}
