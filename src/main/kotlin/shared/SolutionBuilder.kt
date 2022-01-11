package shared

class SolutionBuilder : Comparable<SolutionBuilder> {
    val routes = mutableListOf<RouteBuilder>()

    private val instance: Instance
    private val unvisitedNodes: MutableSet<Node>

    constructor(instance: Instance) : this(
        instance,
        HashSet<Node>(instance.nodes).also { it.remove(instance.depot) }  // do not count depot
    )

    private constructor(instance: Instance, unvisitedNodes: MutableSet<Node>) {
        this.instance = instance
        this.unvisitedNodes = unvisitedNodes
    }

    val currentRoute get() = routes.last()
    val vehiclesUsed get() = routes.size
    val isFinished get() = unvisitedNodes.isEmpty()
    val totalDistance get() = routes.sumOf { it.totalDistance }

    fun createNewRoute() = routes.add(RouteBuilder(instance))

    fun addNextNode(node: Node) = currentRoute.addNextNode(node, unvisitedNodes)

    fun findNeighboringCustomers(): List<Node> {
        return unvisitedNodes.filter { node -> currentRoute.isValidNextNode(node, unvisitedNodes) }
    }

    override fun compareTo(other: SolutionBuilder) =
        compareValuesBy(this, other, { it.vehiclesUsed }, { it.totalDistance })

    fun deepCopy(): SolutionBuilder {
        val builder = SolutionBuilder(instance, HashSet(unvisitedNodes))
        for (route in routes) {
            builder.routes.add(route.deepCopy())
        }
        return builder
    }
}
