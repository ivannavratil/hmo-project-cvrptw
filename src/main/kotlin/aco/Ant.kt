package aco

import helpers.Config
import helpers.FlatSquareMatrix
import helpers.WeightedLottery
import helpers.argmax
import helpers.seededRandom
import heuristics.WaitHeuristic
import shared.Instance
import shared.Node
import shared.NodeMeta
import shared.SolutionBuilder
import kotlin.math.pow

class Ant(
    private val instance: Instance,
    private val pheromones: FlatSquareMatrix,
    private val antConfig: Config.Ant
) {
    private val waitHeuristic = WaitHeuristic(instance)

    fun traverse(): SolutionBuilder? {
        val solutionBuilder = SolutionBuilder(instance)

        solutionBuilder.createNewRoute()
        while (true) {
            val neighbors = solutionBuilder.findNeighboringCustomers()
            if (neighbors.isEmpty()) {
                solutionBuilder.addNextNode(instance.depot)
                if (solutionBuilder.isFinished) {
                    return solutionBuilder
                }

                solutionBuilder.createNewRoute()
                if (solutionBuilder.vehiclesUsed > instance.numberOfVehicles) {
                    return null
                }
            } else {
                val nextCustomer = pickNextCustomer(solutionBuilder.currentRoute.route.last(), neighbors)
                solutionBuilder.addNextNode(nextCustomer)
            }
        }
    }

    private fun pickNextCustomer(sourceMeta: NodeMeta, neighbors: List<Node>): Node {
        val numerators = DoubleArray(neighbors.size) { calculateNumerators(sourceMeta, neighbors[it]) }

        val chosenIndex = if (seededRandom.nextDouble() <= antConfig.q0) {
            numerators.argmax()
        } else {
            WeightedLottery(numerators).draw()
        }
        return neighbors[chosenIndex]
    }

    private fun calculateNumerators(sourceMeta: NodeMeta, destination: Node): Double {
        val pheromones = pheromones[sourceMeta.node.id, destination.id].pow(antConfig.alpha)
        val visibility = 1.0 / (instance.distances[sourceMeta.node.id, destination.id]).pow(antConfig.beta)
        val waitTime = waitHeuristic.calculateWaitTime(sourceMeta, destination).pow(antConfig.theta)
        return pheromones * visibility * waitTime
    }

}
