package aco

import helpers.*
import heuristics.WaitHeuristic
import shared.Instance
import shared.Node
import shared.NodeMeta
import shared.SolutionBuilder
import kotlin.math.abs
import kotlin.math.pow

class Ant(
    private val instance: Instance,
    private val pheromones: FlatSquareMatrix,
    private val antConfig: Config.Ant
) {
    private val waitHeuristic = WaitHeuristic(instance)
//    private val savingsHeuristic = SavingsHeuristic(instance)

    // TODO Update local pheromones etc
    fun traverse(): SolutionBuilder? {
        val solutionBuilder = SolutionBuilder(instance)

        solutionBuilder.createNewRoute()
        while (true) {
            val neighbors = solutionBuilder.findNeighboringCustomers()
            if (neighbors.isEmpty()) {
                solutionBuilder.currentRoute.addNextNode(instance.depot)
                if (solutionBuilder.isFinished) {
                    return solutionBuilder  // TODO local search
                }

                solutionBuilder.createNewRoute()
                if (solutionBuilder.vehiclesUsed > instance.numberOfVehicles) {
                    return null
                }
            } else {
                val nextCustomer = pickNextCustomer(solutionBuilder.currentRoute.route.last(), neighbors)
                solutionBuilder.currentRoute.addNextNode(nextCustomer)
            }
        }
    }

    private fun pickNextCustomer(sourceMeta: NodeMeta, neighbors: List<Node>): Node {
        val numerators = DoubleArray(neighbors.size) { calculateNumerators(sourceMeta, neighbors[it]) }

        val chosenIndex = if (seededRandom.nextDouble() <= antConfig.q0) {
            numerators.argmax()
        } else {
            //TODO: WeightedLottery doesn't throw but total solution is worse :(
            val min = numerators.minOf { it }
            if (min < 0) {
                val absMin = abs(min)
                numerators.indices.forEach { numerators[it] += absMin }
            }
            WeightedLottery(numerators).draw()
        }
        return neighbors[chosenIndex]
    }

    private fun calculateNumerators(sourceMeta: NodeMeta, destination: Node): Double {
        val pheromones = pheromones[sourceMeta.node.id, destination.id].pow(antConfig.alpha)
        val visibility = instance.inverseDistances[sourceMeta.node.id, destination.id].pow(antConfig.beta)
        val waitTime = waitHeuristic.calculateWaitTime(sourceMeta, destination).pow(antConfig.theta)
        return pheromones * visibility * waitTime

//        val savings = (savingsHeuristic.calculateSavings(sourceMeta.node.id, destination.id) + 1).pow(antConfig.lambda)
//        return pheromones * visibility * savings * waitTime
    }

}
