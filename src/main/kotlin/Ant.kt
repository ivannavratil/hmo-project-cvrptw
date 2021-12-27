import Distances.calculateTravelTime
import Distances.inverseDistances
import SavingsHeuristic.calculateSavings
import WaitHeuristic.calculateWaitTime
import org.jetbrains.bio.viktor.F64Array
import java.lang.Integer.max
import kotlin.math.pow

class Ant(
    val instance: Instance,
    val pheromones: F64Array,
    private val alpha: Double = 1.0,
    private val beta: Double = 2.0,
    private val lambda: Double = 3.0,
    private val theta: Double = 0.75
) {
    fun traverse(): SolutionBuilder? {
        val solutionBuilder = SolutionBuilder()

        while (true) {
            val neighbors = solutionBuilder.findNeighbors()
            if (neighbors.isEmpty()) {
                solutionBuilder.currentRoute.addNextNode(instance.depot)
                if (solutionBuilder.isFinished)
                    return solutionBuilder

                solutionBuilder.createNewRoute()
                if (solutionBuilder.vehiclesUsed > instance.numberOfVehicles)
                    return null
            } else {
                // TODO Pick node! Update pheromones etc
            }
        }
    }

    fun calculateProbability(source: NodeMeta, destination: Node): Double {
        return pheromones[source.node.id, destination.id].pow(alpha) *
                inverseDistances[source.node.id, destination.id].pow(beta) *
                calculateSavings(source.node.id, destination.id).pow(lambda) *
                calculateWaitTime(source, destination).pow(theta)
    }


    // TODO look for neighbors only in set of unvisited nodes?
    inner class SolutionBuilder {
        val routes = mutableListOf(RouteBuilder())
        val visitedNodes = F64Array(instance.nodes.size) { 0.0 }  // TODO Switch to set or regular array?
        var unvisitedNodesCount = instance.nodes.size - 1  // do not count depot

        val currentRoute get() = routes.last()
        val vehiclesUsed get() = routes.size
        val isFinished get() = unvisitedNodesCount <= 0

        fun createNewRoute() = routes.add(RouteBuilder())

        fun findNeighbors(): List<Node> {
            return instance.nodes.filter { node ->
                node !== instance.depot && currentRoute.isValidNextNode(node)
            }
        }


        inner class RouteBuilder {
            var remainingCapacity = instance.capacity
            val route = mutableListOf(with(instance.depot) {
                NodeMeta(this, this.readyTime, this.readyTime + this.serviceTime)
            })

            // TODO Do not call this with depot as node! -- if (node === instance.depot) return false ? (findNeighbors)
            // TODO Can this be vectorized?
            fun isValidNextNode(node: Node): Boolean {
                //has this node been visited before
                if (visitedNodes[node.id] > 0.5)
                    return false

                if (remainingCapacity < node.demand)
                    return false

                val lastNodeMeta = route.last()
                val arrivalTime = lastNodeMeta.departureTime + calculateTravelTime(lastNodeMeta.node.id, node.id)
                //will the vehicle arrive before customer is closed
                if (arrivalTime > node.dueTime)
                    return false

                val depotArrival = max(arrivalTime, node.readyTime) +
                        node.serviceTime +
                        calculateTravelTime(node.id, instance.depot.id)
                //will the vehicle manage to return to the depot
                if (depotArrival > instance.depot.dueTime)
                    return false

                return true
            }

            fun addNextNode(node: Node) {
                // TODO Extract method for arrivalTime?
                val lastNodeMeta = route.last()
                val arrivalTime = lastNodeMeta.departureTime + calculateTravelTime(lastNodeMeta.node.id, node.id)
                val departureTime = max(arrivalTime, node.readyTime) + node.serviceTime
                route.add(NodeMeta(node, arrivalTime, departureTime))

                if (node === instance.depot)
                    return

                if (!isValidNextNode(node)) throw RuntimeException("¡Ay, caramba!")  // TODO comment out

                visitedNodes[node.id] = 1.0
                unvisitedNodesCount--

                remainingCapacity -= node.demand
            }
        }
    }
}
