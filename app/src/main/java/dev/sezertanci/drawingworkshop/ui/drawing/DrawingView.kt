package dev.sezertanci.drawingworkshop.ui.drawing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import dev.sezertanci.drawingworkshop.domain.geometry.Viewport
import dev.sezertanci.drawingworkshop.ui.drawing.engine.DrawingEngine

/**
 * Tuvali ekrana çizen ve dokunmaları ileten ince görünüm. Çizim mantığı [DrawingEngine] içindedir.
 *
 * Tek parmak çizer. İkinci parmak konunca o an çizilen çizgi iptal edilir ve iki parmakla büyütme /
 * kaydırma başlar; parmakların hepsi kalkana kadar çizim yapılmaz. Dokunulan noktalar ekrandan
 * tuvalin piksel koordinatına çevrilerek iletilir. Hızlı hareketlerde sistemin biriktirdiği ara
 * noktalar da kullanılır, böylece çizgiler kesik kesik görünmez.
 */
class DrawingView(context: Context) : View(context) {
    private var engine: DrawingEngine? = null
    private var touchListener: DrawingTouchListener? = null
    private val viewport = Viewport(MAX_ZOOM)
    private val pinch = PinchTracker()
    private var mode = TouchMode.IDLE
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private val frameCallback = Choreographer.FrameCallback(::onFrame)
    private var isFrameLoopRunning = false

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
        stopFrameLoop()
        engine?.setOnChangedListener(null)
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        engine?.setOnChangedListener(::invalidate)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewport.setSize(w, h)
        engine?.resize(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        val engine = engine ?: return
        val saved = canvas.save()
        canvas.translate(viewport.offsetX, viewport.offsetY)
        canvas.scale(viewport.scale, viewport.scale)
        engine.draw(canvas, viewport.scale)
        canvas.restoreToCount(saved)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val listener = touchListener ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> startDrawing(event, listener)
            MotionEvent.ACTION_POINTER_DOWN -> startPinch(event, listener)
            MotionEvent.ACTION_MOVE -> onMove(event, listener)
            MotionEvent.ACTION_POINTER_UP -> if (mode == TouchMode.PINCH) pinch.reset(event, excludeIndex = event.actionIndex)
            MotionEvent.ACTION_UP -> finish(listener)
            MotionEvent.ACTION_CANCEL -> cancel(listener)
        }
        return true
    }

    private fun startDrawing(event: MotionEvent, listener: DrawingTouchListener) {
        requestUnbufferedDispatch(event)
        mode = TouchMode.DRAWING
        activePointerId = event.getPointerId(0)
        listener.onTouchStart(viewport.toCanvasX(event.x), viewport.toCanvasY(event.y), viewport.scale)
        startFrameLoop()
    }

    /**
     * Parmak dursa da ilerleyen çizgiler (ipek) için her ekran karesinde motor ilerletilir. Motor böyle
     * bir çizgi olmadığını bildirince döngü kendiliğinden durur.
     */
    private fun startFrameLoop() {
        if (isFrameLoopRunning) return
        isFrameLoopRunning = true
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun onFrame(frameTimeNanos: Long) {
        isFrameLoopRunning = engine?.advanceFrame(frameTimeNanos) == true
        if (isFrameLoopRunning) Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun stopFrameLoop() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        isFrameLoopRunning = false
    }

    private fun startPinch(event: MotionEvent, listener: DrawingTouchListener) {
        if (mode == TouchMode.DRAWING) listener.onTouchCancel()
        mode = TouchMode.PINCH
        activePointerId = MotionEvent.INVALID_POINTER_ID
        pinch.reset(event)
    }

    private fun onMove(event: MotionEvent, listener: DrawingTouchListener) {
        when (mode) {
            TouchMode.DRAWING -> forwardMove(event, listener)
            TouchMode.PINCH -> pinch.update(event)?.let { step ->
                viewport.transform(step.focusX, step.focusY, step.scaleFactor, step.panX, step.panY)
                invalidate()
            }
            TouchMode.IDLE -> Unit
        }
    }

    private fun forwardMove(event: MotionEvent, listener: DrawingTouchListener) {
        val index = event.findPointerIndex(activePointerId)
        if (index < 0) return
        for (h in 0 until event.historySize) {
            listener.onTouchMove(
                viewport.toCanvasX(event.getHistoricalX(index, h)),
                viewport.toCanvasY(event.getHistoricalY(index, h)),
            )
        }
        listener.onTouchMove(viewport.toCanvasX(event.getX(index)), viewport.toCanvasY(event.getY(index)))
        listener.onTouchBatchEnd()
    }

    private fun finish(listener: DrawingTouchListener) {
        if (mode == TouchMode.DRAWING) listener.onTouchEnd()
        mode = TouchMode.IDLE
        activePointerId = MotionEvent.INVALID_POINTER_ID
    }

    private fun cancel(listener: DrawingTouchListener) {
        if (mode == TouchMode.DRAWING) listener.onTouchCancel()
        mode = TouchMode.IDLE
        activePointerId = MotionEvent.INVALID_POINTER_ID
    }

    private enum class TouchMode { IDLE, DRAWING, PINCH }

    private companion object {
        const val CORNER_RADIUS_DP = 16f
        const val MAX_ZOOM = 4f
    }
}
