package dev.sezertanci.drawingworkshop.domain.stroke

/**
 * Kutulama (boxing) yapmadan büyüyen Float dizisi.
 * Sprey noktaları gibi sık eklenen verilerde çöp toplayıcı yükünü azaltır.
 */
class FloatList(initialCapacity: Int = DEFAULT_CAPACITY) {
    var values: FloatArray = FloatArray(initialCapacity)
        private set
    var size: Int = 0
        private set

    fun add(value: Float) {
        if (size == values.size) values = values.copyOf(values.size * GROWTH_FACTOR)
        values[size++] = value
    }

    fun clear() {
        size = 0
    }

    private companion object {
        const val DEFAULT_CAPACITY = 256
        const val GROWTH_FACTOR = 2
    }
}
