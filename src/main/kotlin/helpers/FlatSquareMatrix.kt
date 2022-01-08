package helpers

import kotlin.math.ceil

class FlatSquareMatrix {

    private val stride: Int
    private val data: DoubleArray

    constructor(stride: Int, initializer: (row: Int, col: Int) -> Double) {
        this.stride = stride
        this.data = DoubleArray(stride * stride) { i ->
            initializer(i / stride, i % stride)
        }
    }

    private constructor(stride: Int, data: DoubleArray) {
        this.data = data
        this.stride = stride
    }

    operator fun get(r: Int, c: Int): Double = data[r * stride + c]

    // Used for calculating travel time. Not cached because of matrix size.
    fun getCeil(r: Int, c: Int): Int = ceil(data[r * stride + c]).toInt()

    operator fun set(r: Int, c: Int, value: Double) {
        data[r * stride + c] = value
    }

    operator fun timesAssign(factor: Double) {
        for (i in data.indices)
            data[i] *= factor
    }

    fun copy() = FlatSquareMatrix(stride, data.clone())

}
