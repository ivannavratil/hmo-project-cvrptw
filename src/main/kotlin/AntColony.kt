import org.jetbrains.bio.viktor.F64Array
import org.jetbrains.bio.viktor.div
import kotlin.math.hypot

class AntColony(
    val tauZero: Double,  // TODO set to 1/L for random first try?
    val instance: Instance
) {
    val pheromones = F64Array(instance.nodes.size, instance.nodes.size) { _, _ -> tauZero }

    fun antDo() {
        // TODO make parallelizable
    }

    companion object {
        fun calculateDistances(instance: Instance): Pair<F64Array, F64Array> {
            val distances = F64Array(instance.nodes.size, instance.nodes.size) { row, col ->
                val n1 = instance.nodes[row]
                val n2 = instance.nodes[col]
                hypot(n2.xCoordinate - n1.xCoordinate.toDouble(), n2.yCoordinate - n1.yCoordinate.toDouble())
            }
            return Pair(distances, 1.0 / distances)  // TODO Infinity on diagonal (NaN after *= 0.0)
        }
    }
}
