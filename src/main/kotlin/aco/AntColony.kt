package aco

import helpers.Config
import helpers.Distances
import helpers.seededRandom
import org.apache.logging.log4j.LogManager
import org.jetbrains.bio.viktor.F64Array
import shared.Instance
import kotlin.math.exp

class AntColony(
    private val instance: Instance,
    private val antColonyConfig: Config.AntColony
) {
    var incumbentSolution: Ant.SolutionBuilder? = null

    // TODO Set tauZero to 1/L for random first try?
    private val pheromones = F64Array(instance.nodes.size, instance.nodes.size) { _, _ -> antColonyConfig.tauZero }
    private var currentTemperature = antColonyConfig.simulatedAnnealing.startingTemperature
    private val logger = LogManager.getLogger(this::class.java.simpleName)

    init {
        Distances.initDistances(instance)
    }

    private fun performSingleIteration(
        antConfig: Config.Ant
    ): Boolean {
        val pheromonesLocal = pheromones.copy()

        val solutions = mutableListOf<Ant.SolutionBuilder>()
        repeat(antConfig.count) {  // TODO parallelize or use local pheromones (ACS)
            val ant = Ant(instance, pheromonesLocal, antConfig)
            ant.traverse()?.let { solution ->
                solutions.add(solution)
                evaporatePheromones(pheromonesLocal, antConfig.rho)
                updatePheromones(pheromonesLocal, solution, antConfig.rho, 1)
            }
        }

        //all ants failed to found a solution
        if (solutions.isEmpty()) {
            return false
        }

        val bestAnt = solutions.minOf { it }

        //for the first iteration
        if (incumbentSolution == null) {
            logger.info("Found new best solution - vehicles: ${bestAnt.vehiclesUsed}, distance: ${bestAnt.totalDistance}")
            incumbentSolution = bestAnt
        }

        val antsThatCanLayPheromones: List<Ant.SolutionBuilder> = solutions.filter {
            val incumbentVehicles = incumbentSolution!!.vehiclesUsed
            val incumbentDistance = incumbentSolution!!.totalDistance

            val vehicleDifference = it.vehiclesUsed - incumbentVehicles
            val distanceDifference = it.totalDistance - incumbentDistance

            if (vehicleDifference < 0 || (vehicleDifference == 0 && distanceDifference < 0)) {
                return@filter true
            }

            val ds = incumbentDistance - it.totalDistance * (1 + vehicleDifference * 0.2)

            if (ds > 0) {
                logger.error("ds: ${ds / currentTemperature}")
            }

            return@filter seededRandom.nextDouble() < exp(ds / currentTemperature)
        }

        currentTemperature = antColonyConfig.simulatedAnnealing.decrement!!.decrement(currentTemperature)

        evaporatePheromones(pheromones, antConfig.rho)
        antsThatCanLayPheromones.forEach { solution ->
            updatePheromones(pheromones, solution, antConfig.rho, antsThatCanLayPheromones.size)
        }

        if (bestAnt < incumbentSolution!!) {
            logger.info("Found new best solution - vehicles: ${bestAnt.vehiclesUsed}, distance: ${bestAnt.totalDistance}")
            //logger.trace(Solution.fromSolutionBuilder(bestAnt).formatOutput())
            incumbentSolution = bestAnt
        }

        return true
    }

    private fun evaporatePheromones(pheromones: F64Array, rho: Double) {
        pheromones *= (1 - rho)
    }

    private fun updatePheromones(pheromones: F64Array, solution: Ant.SolutionBuilder, rho: Double, antCount: Int) {
        val pheromoneDelta = 1.0 / (solution.totalDistance * antCount)
        solution.routes.forEach { antTraversal ->
            antTraversal.route.zipWithNext().forEach { pair ->
                val id1 = pair.first.node.id
                val id2 = pair.second.node.id
                pheromones[id1, id2] += rho * pheromoneDelta
            }
        }
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
