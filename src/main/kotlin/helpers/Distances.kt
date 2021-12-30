package helpers

import shared.Instance
import kotlin.math.ceil
import kotlin.math.hypot

object Distances {
    lateinit var distances: FlatSquareMatrix
    lateinit var inverseDistances: FlatSquareMatrix
    lateinit var travelTime: FlatSquareMatrixInt

    fun initDistances(instance: Instance) {
        distances = FlatSquareMatrix(instance.nodes.size) { row, col ->
            val n1 = instance.nodes[row]
            val n2 = instance.nodes[col]
            hypot(n2.xCoordinate - n1.xCoordinate, n2.yCoordinate - n1.yCoordinate)
        }
        // TODO Infinity on diagonal (NaN after *= 0.0)
        inverseDistances = distances.copy().transformInPlace { 1.0 / it }
        travelTime = FlatSquareMatrixInt(instance.nodes.size) { row, col -> ceil(distances[row, col]).toInt() }
    }

}
