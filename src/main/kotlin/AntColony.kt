import org.jetbrains.bio.viktor.F64Array

class AntColony(
    val tauZero: Double,  // TODO set to 1/L for random first try?
    val instance: Instance
) {
    val pheromones: F64Array = F64Array(instance.nodes.size, instance.nodes.size) { _, _ -> tauZero }

    init {
        Distances.initDistances(instance)
    }

    fun antDo() {
        // TODO make parallelizable
    }
}
