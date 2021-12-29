package aco

import helpers.Config
import helpers.Distances.calculateTravelTime
import helpers.Distances.distances
import helpers.Distances.inverseDistances
import helpers.WeightedLottery
import helpers.argmax
import helpers.seededRandom
import heuristics.SavingsHeuristic.calculateSavings
import heuristics.WaitHeuristic.calculateWaitTime
import org.jetbrains.bio.viktor.F64Array
import shared.Instance
import shared.Node
import shared.NodeMeta
import kotlin.math.abs
import kotlin.math.pow

class Ant(
    private val instance: Instance,
    private val pheromones: F64Array,
    private val antConfig: Config.Ant
) {
    // TODO Update local pheromones etc
    fun traverse(): SolutionBuilder? {
        val solutionBuilder = SolutionBuilder()

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
            numerators.asIterable().argmax()!!
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
        val visibility = inverseDistances[sourceMeta.node.id, destination.id].pow(antConfig.beta)
        val savings = 1.0  // calculateSavings(sourceMeta.node.id, destination.id).pow(antConfig.lambda)
        val waitTime = calculateWaitTime(sourceMeta, destination).pow(antConfig.theta)
        return pheromones * visibility * savings * waitTime
    }

    // TODO look for neighbors only in set of unvisited nodes?
    inner class SolutionBuilder : Comparable<SolutionBuilder> {
        val routes = mutableListOf(RouteBuilder())
        val visitedNodes = F64Array(instance.nodes.size) { 0.0 }  // TODO Switch to set or regular array?
        var unvisitedNodesCount = instance.nodes.size - 1  // do not count depot

        val currentRoute get() = routes.last()
        val vehiclesUsed get() = routes.size
        val isFinished get() = unvisitedNodesCount <= 0
        val totalDistance get() = routes.sumOf { it.totalDistance }  // TODO recalculated many times

        fun createNewRoute() = routes.add(RouteBuilder())

        fun findNeighboringCustomers(): List<Node> {
            return instance.nodes.filter { node ->
                node !== instance.depot && currentRoute.isValidNextNode(node)
            }
        }

        override fun compareTo(other: SolutionBuilder) =
            compareValuesBy(this, other, { it.vehiclesUsed }, { it.totalDistance })

        inner class RouteBuilder {
            var remainingCapacity = instance.capacity
            var totalDistance = 0.0
            val route = mutableListOf(with(instance.depot) {
                NodeMeta(this, this.readyTime, this.readyTime + this.serviceTime)
            })

            // TODO Do not call this with depot as node! -- if (node === instance.depot) return false ? (findNeighboringCustomers)
            // TODO Can this be vectorized?
            fun isValidNextNode(node: Node): Boolean {
                // Has this node been visited before
                if (visitedNodes[node.id] > 0.5)
                    return false

                if (remainingCapacity < node.demand)
                    return false

                // Will the vehicle arrive before customer is closed
                val lastNodeMeta = route.last()
                val arrivalTime = lastNodeMeta.departureTime + calculateTravelTime(lastNodeMeta.node.id, node.id)
                if (arrivalTime > node.dueTime)
                    return false

                // Will the vehicle manage to return to the depot
                val depotArrival = maxOf(arrivalTime, node.readyTime) +
                        node.serviceTime +
                        calculateTravelTime(node.id, instance.depot.id)
                if (depotArrival > instance.depot.dueTime)
                    return false

                return true
            }

            fun addNextNode(node: Node) {
                if (node !== instance.depot && !isValidNextNode(node))
                    throw RuntimeException("¡Ay, caramba!")  // TODO comment out

                // TODO Extract method for arrivalTime?
                val lastNodeMeta = route.last()
                val arrivalTime = lastNodeMeta.departureTime + calculateTravelTime(lastNodeMeta.node.id, node.id)
                val departureTime = maxOf(arrivalTime, node.readyTime) + node.serviceTime  // TODO extract maxOf?
                route.add(NodeMeta(node, arrivalTime, departureTime))
                totalDistance += distances[lastNodeMeta.node.id, node.id]

                if (node === instance.depot)
                    return

                visitedNodes[node.id] = 1.0
                unvisitedNodesCount--

                remainingCapacity -= node.demand
            }
        }
    }
}
