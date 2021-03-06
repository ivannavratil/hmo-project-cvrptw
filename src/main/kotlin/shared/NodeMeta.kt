package shared

// Must be immutable.
data class NodeMeta(
    val node: Node,
    val serviceStartTime: Int,
    val departureTime: Int  // when servicing is done, for caching purposes
) {
    fun calculateNext(node: Node, instance: Instance): NodeMeta {
        val arrivalTime = this.departureTime + instance.distances.getCeil(this.node.id, node.id)
        val serviceStartTime = maxOf(arrivalTime, node.readyTime)
        val departureTime = serviceStartTime + node.serviceTime
        return NodeMeta(node, serviceStartTime, departureTime)
    }
}
