import org.jetbrains.bio.viktor.F64Array

class AntColony(
    val instance: Instance,
    val tauZero: Double,  // TODO set to 1/L for random first try?
    private val startingTemperature: Double
) {
    val pheromones: F64Array = F64Array(instance.nodes.size, instance.nodes.size) { _, _ -> tauZero }
    var currentTemperature = startingTemperature

    init {
        Distances.initDistances(instance)
    }

    fun performSingleIteration(
        antCount: Int = 10,
        alpha: Double = 1.0,
        beta: Double = 2.0,
        lambda: Double = 3.0,
        theta: Double = 0.75,
        q0: Double = 0.75,
        rho: Double = 0.1
    ) {
        // TODO Simulated annealing

        val solutions = mutableListOf<Ant.SolutionBuilder>()
        repeat(antCount) {  // TODO parallelize
            val ant = Ant(instance, pheromones, alpha, beta, lambda, theta, q0)
            ant.traverse()?.let(solutions::add)
        }

        val bestSolution = solutions.maxWithOrNull(compareBy({ it.vehiclesUsed }, { it.totalDistance }))
    }

}
