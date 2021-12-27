import org.jetbrains.bio.viktor.F64Array

class AntColony(
    val instance: Instance,
    val tauZero: Double = 1e-5,  // TODO set to 1/L for random first try?
    startingTemperature: Double
) {
    val pheromones: F64Array = F64Array(instance.nodes.size, instance.nodes.size) { _, _ -> tauZero }
    var currentTemperature = startingTemperature
    var incumbentSolution: Ant.SolutionBuilder? = null

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
    ): Ant.SolutionBuilder? {
        // TODO Simulated annealing

        val solutions = mutableListOf<Ant.SolutionBuilder>()
        repeat(antCount) {  // TODO parallelize
            val ant = Ant(instance, pheromones, alpha, beta, lambda, theta, q0)
            ant.traverse()?.let(solutions::add)
        }

        // TODO e.g. pick best 2?
        val bestSolution = solutions.maxOfOrNull { it } ?: return null

        val pheromoneDelta = 1.0 / bestSolution.totalDistance
        pheromones *= (1 - rho)
        bestSolution.routes.forEach {
            it.route.zipWithNext().forEach { pair ->
                val id1 = pair.first.node.id
                val id2 = pair.second.node.id
                pheromones[id1, id2] += rho * pheromoneDelta
            }
        }

        if (incumbentSolution == null || incumbentSolution!! < bestSolution) {
            println("Found new best solution")
            incumbentSolution = bestSolution
        }

        println("${bestSolution.vehiclesUsed} ${bestSolution.totalDistance}")

        return bestSolution
    }

}
