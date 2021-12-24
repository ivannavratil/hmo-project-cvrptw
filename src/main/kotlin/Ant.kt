import org.jetbrains.bio.viktor.F64Array
import java.lang.Integer.max
import kotlin.math.ceil

class Ant(
    val instance: Instance,
    val distances: F64Array,
    val inverseDistances: F64Array,
    val pheromones: F64Array
) {
    fun traverse() {
        val solution = SolutionBuilder()
        // TODO Check that all nodes are visited and that #routes <= instance.numberOfVehicles

//        solution.routes.last().isValidNextNode(node)
    }

    private fun calculateTravelTime(id1: Int, id2: Int) = ceil(distances[id1, id2]).toInt()

    inner class SolutionBuilder {
        val visitedNodes = F64Array(instance.nodes.size) { 0.0 }  // TODO Switch to set or regular array?
        var visitedNodesCount = 0
        val routes = mutableListOf(RouteBuilder())  // TODO Add new route when finished

        inner class RouteBuilder {
            var remainingCapacity = instance.capacity
            val route = mutableListOf(with(instance.depot) {
                NodeMeta(this, this.readyTime, this.readyTime + this.serviceTime)
            })

            // TODO Do not call this with depot as node!
            // TODO Can this be vectorized?
            fun isValidNextNode(node: Node): Boolean {
                if (visitedNodes[node.id] > 0.5)
                    return false

                if (remainingCapacity < node.demand)
                    return false

                val lastNodeMeta = route.last()
                val arrivalTime = lastNodeMeta.departureTime + calculateTravelTime(lastNodeMeta.node.id, node.id)
                if (arrivalTime > node.dueTime)
                    return false

                val depotArrival = max(arrivalTime, node.readyTime) +
                        node.serviceTime +
                        calculateTravelTime(node.id, instance.depot.id)
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
                visitedNodesCount++

                remainingCapacity -= node.demand
            }
        }
    }
}
