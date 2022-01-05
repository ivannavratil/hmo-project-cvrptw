package shared

data class NodeMeta(
    val node: Node,
    val serviceStartTime: Int,
    val departureTime: Int  // when servicing is done, for caching purposes
) {
    fun calculateNext(node: Node, instance: Instance): NodeMeta {
        val arrivalTime = this.departureTime + instance.travelTime[this.node.id, node.id]
        val serviceStartTime = maxOf(arrivalTime, node.readyTime)
        val departureTime = serviceStartTime + node.serviceTime
        return NodeMeta(node, serviceStartTime, departureTime)
    }
}
