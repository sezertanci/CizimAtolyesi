package dev.sezertanci.drawingworkshop.domain.history

/**
 * Sınırlı kapasiteli geri al / yinele geçmişi.
 *
 * Her kayıt, uygulandığında kendi tersini üreten bir girdidir: geri alınan girdi uygulanır ve dönen
 * ters girdi yinele yığınına konur. Böylece aynı anda bellekte en fazla [capacity] girdi tutulur.
 * Kapasiteyi aşan veya geçersiz kalan girdiler [onDiscard] ile bırakılır (ör. bitmap'lerin geri verilmesi).
 */
class UndoRedoHistory<T>(
    private val capacity: Int,
    private val onDiscard: (T) -> Unit = {},
) {
    private val undoStack = ArrayDeque<T>()
    private val redoStack = ArrayDeque<T>()

    init {
        require(capacity > 0) { "Kapasite pozitif olmalı." }
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun record(entry: T) {
        discardAll(redoStack)
        undoStack.addLast(entry)
        if (undoStack.size > capacity) onDiscard(undoStack.removeFirst())
    }

    /** Son girdiyi [apply] ile uygular; dönen ters girdi yinele yığınına eklenir. */
    fun undo(apply: (T) -> T): Boolean = move(undoStack, redoStack, apply)

    /** Son geri alınan girdiyi [apply] ile uygular; dönen ters girdi geri al yığınına eklenir. */
    fun redo(apply: (T) -> T): Boolean = move(redoStack, undoStack, apply)

    fun clear() {
        discardAll(undoStack)
        discardAll(redoStack)
    }

    private fun move(from: ArrayDeque<T>, to: ArrayDeque<T>, apply: (T) -> T): Boolean {
        val entry = from.removeLastOrNull() ?: return false
        to.addLast(apply(entry))
        return true
    }

    private fun discardAll(stack: ArrayDeque<T>) {
        stack.forEach(onDiscard)
        stack.clear()
    }
}
