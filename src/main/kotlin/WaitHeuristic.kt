import Distances.calculateTravelTime

class WaitHeuristic(private val instance: Instance) {

    fun calculateWaitTime(sourceNodeMeta: NodeMeta, destinationId: Int): Double {
        val arrivalTime = sourceNodeMeta.departureTime + calculateTravelTime(sourceNodeMeta.node.id, destinationId)
        val readyTime = instance.nodes[destinationId].readyTime
        if (readyTime <= arrivalTime)
            return 1.0
        return 1.0 / (readyTime - arrivalTime)
    }

}
