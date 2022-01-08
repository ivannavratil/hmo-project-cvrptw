package aco

import helpers.*
import local.LocalSearch
import org.apache.logging.log4j.LogManager
import shared.Instance
import shared.SolutionBuilder
import java.time.Duration
import java.time.Instant

class AntColony(
    private val instance: Instance,
    config: Config
) {
    var incumbentSolution: SolutionBuilder? = null
        private set
    var incumbentEvaluations = 0
        private set
    lateinit var incumbentTime: Duration
        private set
    var evaluations = 0
        private set
    var evaluationsTauZero = 0
        private set

    private var iteration = 0
    private lateinit var pheromones: FlatSquareMatrix
    private val config = config.deepCopy()

    private lateinit var startTime: Instant
    private val logger = LogManager.getLogger(this::class.java.simpleName)

    private fun performSingleIteration(
        antConfig: Config.Ant
    ): Boolean {
        iteration++
        val pheromonesLocal = pheromones.copy()

        val solutions = ArrayList<SolutionBuilder>()
        repeat(antConfig.count) {
            evaluations++
            val ant = Ant(instance, pheromonesLocal, antConfig)
            ant.traverse()?.let { solution ->
                solutions.add(solution)
                evaporatePheromones(pheromonesLocal, antConfig.rho)
                updatePheromones(pheromonesLocal, solution, antConfig.rho)
            }
        }

        //all ants failed to found a solution
        if (solutions.isEmpty()) {
            return false
        }

        // TODO use config?
        // val bestAnt = solutions.minOf { it }
        val bestAnt = LocalSearch(instance, solutions.minOf { it })
            .quickSearch(Config.LocalSearch(8, Duration.ofMillis(100)))

        if (incumbentSolution == null || bestAnt < incumbentSolution!!) {
            logger.info("Found new best solution - vehicles: ${bestAnt.vehiclesUsed}, distance: ${bestAnt.totalDistance}")
            //logger.trace(Solution.fromSolutionBuilder(bestAnt).formatOutput())
            incumbentSolution = bestAnt
            incumbentEvaluations = evaluations
            incumbentTime = Duration.between(startTime, Instant.now())
        }

        evaporatePheromones(pheromones, antConfig.rho)
        updatePheromones(pheromones, bestAnt, antConfig.rho)

        return true
    }

    private fun evaporatePheromones(pheromones: FlatSquareMatrix, rho: Double) {
        pheromones *= 1 - rho
    }

    private fun updatePheromones(pheromones: FlatSquareMatrix, solution: SolutionBuilder, rho: Double) {
        val pheromoneDelta = 1.0 / solution.totalDistance
        solution.routes.forEach { antTraversal ->
            antTraversal.route.zipWithNext().forEach { pair ->
                val id1 = pair.first.node.id
                val id2 = pair.second.node.id
                pheromones[id1, id2] += rho * pheromoneDelta
            }
        }
    }

    fun run(): SolutionBuilder? {
        startTime = Instant.now()
        val compositeTermination = CompositeTermination(
            TotalTimeTermination(config.antColony.runtime),
            TotalIterationsTermination(config.antColony.iterations)
        )

        if (config.antColony.estimateTauZero) {
            config.antColony.tauZero = calculateTauZero(config.antColony.estimateLocalSearch!!)
            logger.info("Tau zero set to ${config.antColony.tauZero}")
        }
        pheromones = FlatSquareMatrix(instance.nodes.size) { _, _ -> config.antColony.tauZero }

        while (!compositeTermination.terminate(iteration)) {
            if (iteration % 50 == 0) {
                logger.trace("iter #$iteration")
            }
            performSingleIteration(config.ant)
        }

        return incumbentSolution
    }

    private fun calculateTauZero(lsConfig: Config.LocalSearch): Double {
        val ant = Ant(instance, FlatSquareMatrix(instance.nodes.size) { _, _ -> config.antColony.tauZero }, config.ant)

        repeat(50) {
            evaluationsTauZero++
            ant.traverse()?.let { solution ->
                val ls = LocalSearch(instance, solution)
                ls.quickSearch(lsConfig)
                evaluationsTauZero += ls.incumbentEvaluations
                return 1 / ls.incumbentSolution.totalDistance
            }
        }

        logger.info("Failed to estimate tau zero!")
        return config.antColony.tauZero
    }
}
