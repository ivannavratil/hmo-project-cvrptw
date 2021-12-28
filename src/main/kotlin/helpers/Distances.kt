package helpers

import org.jetbrains.bio.viktor.F64Array
import org.jetbrains.bio.viktor.div
import shared.Instance
import kotlin.math.ceil
import kotlin.math.hypot

object Distances {
    lateinit var distances: F64Array
    lateinit var inverseDistances: F64Array

    fun initDistances(instance: Instance) {
        distances = F64Array(instance.nodes.size, instance.nodes.size) { row, col ->
            val n1 = instance.nodes[row]
            val n2 = instance.nodes[col]
            hypot(n2.xCoordinate - n1.xCoordinate, n2.yCoordinate - n1.yCoordinate)
        }
        inverseDistances = 1.0 / distances  // TODO Infinity on diagonal (NaN after *= 0.0)
    }

    fun calculateTravelTime(id1: Int, id2: Int) = ceil(distances[id1, id2]).toInt()
}
