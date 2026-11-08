package dev.sezertanci.drawingworkshop.ui.drawing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import dev.sezertanci.drawingworkshop.ui.drawing.engine.DrawingEngine

/**
 * Tuvali ekrana çizen ve dokunmaları ileten ince görünüm. Çizim mantığı [DrawingEngine] içindedir.
 *
 * Yalnızca ilk parmak çizer; ikinci bir parmak (ör. avuç içi) yok sayılır. Hızlı hareketlerde
 * sistemin biriktirdiği ara noktalar da kullanılır, böylece çizgiler kesik kesik görünmez.
 */
class DrawingView(context: Context) : View(context) {
    private var engine: DrawingEngine? = null
    private var touchListener: DrawingTouchListener? = null
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    init {
        val cornerRadius = CORNER_RADIUS_DP * resources.displayMetrics.density
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
        clipToOutline = true
    }

    fun bind(engine: DrawingEngine, listener: DrawingTouchListener) {
        touchListener = listener
        if (this.engine === engine) return
        this.engine?.setOnChangedListener(null)
        this.engine = engine
        engine.setOnChangedListener(::invalidate)
        if (width > 0 && height > 0) engine.resize(width, height)
        invalidate()
    }

    override fun onDetachedFromWindow() {
        engine?.setOnChangedListener(null)
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        engine?.setOnChangedListener(::invalidate)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        engine?.resize(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        engine?.draw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val listener = touchListener ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                requestUnbufferedDispatch(event)
                activePointerId = event.getPointerId(0)
                listener.onTouchStart(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> forwardMove(event, listener)
            MotionEvent.ACTION_POINTER_UP -> if (event.getPointerId(event.actionIndex) == activePointerId) finish(listener)
            MotionEvent.ACTION_UP -> finish(listener)
            MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
                listener.onTouchCancel()
            }
        }
        return true
    }

    private fun forwardMove(event: MotionEvent, listener: DrawingTouchListener) {
        val index = event.findPointerIndex(activePointerId)
        if (index < 0) return
        for (h in 0 until event.historySize) {
            listener.onTouchMove(event.getHistoricalX(index, h), event.getHistoricalY(index, h))
        }
        listener.onTouchMove(event.getX(index), event.getY(index))
        listener.onTouchBatchEnd()
    }

    private fun finish(listener: DrawingTouchListener) {
        if (activePointerId == MotionEvent.INVALID_POINTER_ID) return
        activePointerId = MotionEvent.INVALID_POINTER_ID
        listener.onTouchEnd()
    }

    private companion object {
        const val CORNER_RADIUS_DP = 16f
    }
}
