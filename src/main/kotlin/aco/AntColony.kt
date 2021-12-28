package aco

import helpers.Config
import helpers.Distances
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jetbrains.bio.viktor.F64Array
import shared.Instance

class AntColony(
    val instance: Instance,
    val antColonyConfig: Config.AntColony
) {
    // TODO Set tauZero to 1/L for random first try?
    val pheromones: F64Array = F64Array(instance.nodes.size, instance.nodes.size) { _, _ -> antColonyConfig.tauZero }
    var currentTemperature = antColonyConfig.simulatedAnnealing.startingTemperature
    var incumbentSolution: Ant.SolutionBuilder? = null
    var logger: Logger = LogManager.getLogger(this::class.java.simpleName)

    init {
        Distances.initDistances(instance)
    }

    private fun performSingleIteration(
        antConfig: Config.Ant
    ): Ant.SolutionBuilder? {
        // TODO Simulated annealing

        val solutions = mutableListOf<Ant.SolutionBuilder>()
        repeat(antConfig.count) {  // TODO parallelize or use local pheromones (ACS)
            val ant = Ant(instance, pheromones, antConfig)
            ant.traverse()?.let(solutions::add)
        }

        // TODO e.g. pick best 2?
        val bestSolution = solutions.minOfOrNull { it } ?: return null

        val pheromoneDelta = 1.0 / bestSolution.totalDistance
        pheromones *= (1 - antConfig.rho)
        bestSolution.routes.forEach {
            it.route.zipWithNext().forEach { pair ->
                val id1 = pair.first.node.id
                val id2 = pair.second.node.id
                pheromones[id1, id2] += antConfig.rho * pheromoneDelta
            }
        }

        if (incumbentSolution == null || bestSolution < incumbentSolution!!) {
            logger.info("Found new best solution - vehicles: ${bestSolution.vehiclesUsed}, distance: ${bestSolution.totalDistance}")
            incumbentSolution = bestSolution
        }

        return bestSolution
    }

    fun run(config: Config) {
        repeat(config.iterations) {
            if (it % 50 == 0) {
                logger.trace("iter #$it")
            }
            performSingleIteration(config.ant)
        }
    }
}
