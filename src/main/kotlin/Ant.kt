import Distances.calculateTravelTime
import Distances.distances
import Distances.inverseDistances
import SavingsHeuristic.calculateSavings
import WaitHeuristic.calculateWaitTime
import com.wl.SimpleIntWeightedLottery
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

class Ant(
    private val instance: Instance,
    private val pheromones: F64Array,
    private val alpha: Double,
    private val beta: Double,
    private val lambda: Double,
    private val theta: Double,
    private val q0: Double
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
        val numerators = DoubleArray(neighbors.size)
        neighbors.forEachIndexed { i, node ->
            numerators[i] = calculateNumerators(sourceMeta, node)
        }

        val chosenIndex = if (Random.nextDouble() <= q0) {
            numerators.asIterable().argmax()!!
        } else {
            //TODO: SimpleIntWeightedLottery doesn't throw but total solution is worse :(
            val min = numerators.minOf { it }
            if (min < 0) {
                numerators.forEachIndexed { index, value ->
                    numerators[index] = value + abs(min)
                }
            }
            SimpleIntWeightedLottery(numerators).draw()
        }
        return neighbors[chosenIndex]
    }

    private fun calculateNumerators(sourceMeta: NodeMeta, destination: Node): Double {
        return pheromones[sourceMeta.node.id, destination.id].pow(alpha) *
                inverseDistances[sourceMeta.node.id, destination.id].pow(beta) *
                calculateSavings(sourceMeta.node.id, destination.id).pow(lambda) *
                calculateWaitTime(sourceMeta, destination).pow(theta)
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
