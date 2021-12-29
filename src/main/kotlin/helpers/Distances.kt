package helpers

import shared.Instance
import kotlin.math.ceil
import kotlin.math.hypot

object Distances {
    lateinit var distances: Array<DoubleArray>
    lateinit var inverseDistances: Array<DoubleArray>

    fun initDistances(instance: Instance) {
        distances = Array(instance.nodes.size) { row ->
            DoubleArray(instance.nodes.size) { col ->
                val n1 = instance.nodes[row]
                val n2 = instance.nodes[col]
                hypot(n2.xCoordinate - n1.xCoordinate, n2.yCoordinate - n1.yCoordinate)
            }
        }
        inverseDistances = Array(instance.nodes.size) { row ->
            DoubleArray(instance.nodes.size) { col ->
                1.0 / distances[row][col]  // TODO Infinity on diagonal (NaN after *= 0.0)
            }
        }
    }

    fun calculateTravelTime(id1: Int, id2: Int) = ceil(distances[id1][id2]).toInt()
}
