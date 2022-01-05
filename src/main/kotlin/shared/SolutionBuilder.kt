package shared

class SolutionBuilder(private val instance: Instance) : Comparable<SolutionBuilder> {
    val routes = mutableListOf<RouteBuilder>()
    private val unvisitedNodes: MutableSet<Node> = HashSet<Node>(instance.nodes.size).also {
        it.addAll(instance.nodes)
        it.remove(instance.depot)  // do not count depot
    }

    val currentRoute get() = routes.last()
    val vehiclesUsed get() = routes.size
    val isFinished get() = unvisitedNodes.isEmpty()
    val totalDistance get() = routes.sumOf { it.totalDistance }  // TODO recalculated many times

    fun createNewRoute() = routes.add(RouteBuilder())

    fun findNeighboringCustomers(): List<Node> {
        return unvisitedNodes.filter { node -> currentRoute.isValidNextNode(node) }
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
        fun isValidNextNode(node: Node): Boolean {
            // Has this node been visited before  // TODO not needed
            if (!unvisitedNodes.contains(node))
                return false

            if (remainingCapacity < node.demand)
                return false

            // Will the vehicle arrive before customer is closed
            val lastNodeMeta = route.last()
            val arrivalTime = lastNodeMeta.departureTime + instance.travelTime[lastNodeMeta.node.id, node.id]
            if (arrivalTime > node.dueTime)
                return false

            // Will the vehicle manage to return to the depot
            val depotArrival = maxOf(arrivalTime, node.readyTime) +
                    node.serviceTime +
                    instance.travelTime[node.id, instance.depot.id]
            if (depotArrival > instance.depot.dueTime)
                return false

            return true
        }

        fun addNextNode(node: Node) {
            if (node !== instance.depot && !isValidNextNode(node))
                throw RuntimeException("¡Ay, caramba!")  // TODO comment out

            // TODO Extract method for arrivalTime?
            val lastNodeMeta = route.last()
            val arrivalTime = lastNodeMeta.departureTime + instance.travelTime[lastNodeMeta.node.id, node.id]
            val departureTime = maxOf(arrivalTime, node.readyTime) + node.serviceTime  // TODO extract maxOf?
            route.add(NodeMeta(node, arrivalTime, departureTime))
            totalDistance += instance.distances[lastNodeMeta.node.id, node.id]

            if (node === instance.depot)
                return

            unvisitedNodes.remove(node)

            remainingCapacity -= node.demand
        }
    }
}
