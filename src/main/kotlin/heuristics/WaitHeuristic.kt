package heuristics

import helpers.Distances.calculateTravelTime
import shared.Node
import shared.NodeMeta

object WaitHeuristic {

    fun calculateWaitTime(sourceNodeMeta: NodeMeta, destination: Node): Double {
        val arrivalTime = sourceNodeMeta.departureTime + calculateTravelTime(sourceNodeMeta.node.id, destination.id)
        if (destination.readyTime <= arrivalTime) {
            return 1.0
        }
        return 1.0 / (destination.readyTime - arrivalTime)
    }

}
