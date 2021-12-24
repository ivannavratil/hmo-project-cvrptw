data class NodeMeta(
    val node: Node,
    val arrivalTime: Int,  // when vehicle arrived, might have to wait until ready time
    val departureTime: Int  // when servicing is done, for caching purposes
)