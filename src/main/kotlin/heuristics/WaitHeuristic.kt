package heuristics

import helpers.Distances.travelTime
import shared.Node
import shared.NodeMeta

object WaitHeuristic {

    fun calculateWaitTime(sourceNodeMeta: NodeMeta, destination: Node): Double {
        val arrivalTime = sourceNodeMeta.departureTime + travelTime[sourceNodeMeta.node.id, destination.id]
        return when {
            destination.readyTime <= arrivalTime -> 1.0
            else -> 1.0 / (destination.readyTime - arrivalTime)
        }
    }

}
