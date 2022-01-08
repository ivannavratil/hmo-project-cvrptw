package heuristics

import shared.Instance
import shared.Node
import shared.NodeMeta

class WaitHeuristic(instance: Instance) {
    private val distances = instance.distances

    fun calculateWaitTime(sourceNodeMeta: NodeMeta, destination: Node): Double {
        val arrivalTime = sourceNodeMeta.departureTime + distances.getCeil(sourceNodeMeta.node.id, destination.id)
        return when {
            destination.readyTime <= arrivalTime -> 1.0
            else -> 1.0 / (destination.readyTime - arrivalTime)
        }
    }

}
