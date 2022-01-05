package shared

class RouteBuilder {
    var remainingCapacity: Int
    var totalDistance: Double
    val route: MutableList<NodeMeta>

    private val instance: Instance

    constructor(instance: Instance) : this(
        instance,
        with(instance.depot) {
            mutableListOf(NodeMeta(this, readyTime, readyTime + serviceTime))
        }
    )

    private constructor(instance: Instance, route: MutableList<NodeMeta>) : this(
        instance, route, instance.capacity, 0.0
    )

    private constructor(
        instance: Instance,
        route: MutableList<NodeMeta>,
        remainingCapacity: Int,
        totalDistance: Double
    ) {
        this.instance = instance
        this.route = route
        this.remainingCapacity = remainingCapacity
        this.totalDistance = totalDistance
    }

    // Do not call this with instance.depot
    fun isValidNextNode(node: Node, unvisitedNodes: Set<Node>): Boolean {
        // Has this node been visited before - check actually not needed
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
        val depotArrival = maxOf(arrivalTime, node.readyTime) + node.serviceTime +
                instance.travelTime[node.id, instance.depot.id]
        if (depotArrival > instance.depot.dueTime)
            return false

        return true
    }

    fun addNextNode(node: Node, unvisitedNodes: MutableSet<Node>) {
        if (node !== instance.depot && !isValidNextNode(node, unvisitedNodes))
            throw RuntimeException("¡Ay, caramba!")  // TODO comment out

        val lastNodeMeta = route.last()
        route.add(lastNodeMeta.calculateNext(node, instance))
        totalDistance += instance.distances[lastNodeMeta.node.id, node.id]

        if (node === instance.depot)
            return

        unvisitedNodes.remove(node)

        remainingCapacity -= node.demand
    }

    fun deepCopy() = RouteBuilder(instance, ArrayList(route), remainingCapacity, totalDistance)

}
