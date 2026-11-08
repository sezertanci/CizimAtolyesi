package dev.sezertanci.drawingworkshop.data.image

import android.graphics.Bitmap
import java.io.OutputStream

private const val PNG_QUALITY = 100

fun Bitmap.writePng(stream: OutputStream): Boolean = compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
