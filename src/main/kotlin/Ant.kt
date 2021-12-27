import org.jetbrains.bio.viktor.F64Array
import java.lang.Integer.max
import kotlin.math.ceil

class Ant(
    val instance: Instance,
    val distances: F64Array,
    val inverseDistances: F64Array,
    val pheromones: F64Array
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

    private fun calculateTravelTime(id1: Int, id2: Int) = ceil(distances[id1, id2]).toInt()


    // TODO look for neighbors only in set of unvisited nodes?
    inner class SolutionBuilder {
        val visitedNodes = F64Array(instance.nodes.size) { 0.0 }  // TODO Switch to set or regular array?
        var unvisitedNodesCount = instance.nodes.size - 1  // do not count depot
        val routes = mutableListOf(RouteBuilder())  // TODO Add new route when finished

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
