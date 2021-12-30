package helpers

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

    operator fun get(r: Int, c: Int): Double {
        return data[r * stride + c]
    }

    operator fun set(r: Int, c: Int, value: Double) {
        data[r * stride + c] = value
    }

    operator fun timesAssign(factor: Double) {
        for (i in data.indices)
            data[i] *= factor
    }

    fun copy() = FlatSquareMatrix(stride, data.clone())

    fun transformInPlace(f: (Double) -> Double): FlatSquareMatrix {
        for (i in data.indices)
            data[i] = f(data[i])
        return this
    }

}


class FlatSquareMatrixInt(private val stride: Int, initializer: (row: Int, col: Int) -> Int) {

    private val data = IntArray(stride * stride) { initializer(it / stride, it % stride) }

    operator fun get(r: Int, c: Int) = data[r * stride + c]

}
