package helpers

import shared.Instance
import kotlin.math.ceil
import kotlin.math.hypot

object Distances {
    lateinit var distances: FlatSquareMatrix
    lateinit var inverseDistances: FlatSquareMatrix

    fun initDistances(instance: Instance) {
        distances = FlatSquareMatrix(instance.nodes.size) { row, col ->
            val n1 = instance.nodes[row]
            val n2 = instance.nodes[col]
            hypot(n2.xCoordinate - n1.xCoordinate, n2.yCoordinate - n1.yCoordinate)
        }
        inverseDistances = distances.copy().transformInPlace { 1.0 / it }  // TODO Infinity on diagonal (NaN after *= 0.0)
    }

    fun calculateTravelTime(id1: Int, id2: Int) = ceil(distances[id1, id2]).toInt()
}
