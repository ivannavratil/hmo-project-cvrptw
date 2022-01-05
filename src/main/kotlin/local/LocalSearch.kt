package local

import helpers.Distances.distances
import helpers.Distances.travelTime
import org.apache.logging.log4j.LogManager
import shared.Instance
import shared.NodeMeta
import shared.SolutionBuilder
import kotlin.math.abs

class LocalSearch(
    private val instance: Instance,
    private val solution: SolutionBuilder  // modified in-place!
) {
    private val logger = LogManager.getLogger(this::class.java.simpleName)

    // TODO Take config as parameter?
    fun search(iterLimit: Int = 2000) {
        var iters = 0
        while (iters++ < iterLimit) {
            if (!iteration())
                break
            logger.info("Found new best solution - vehicles: ${solution.vehiclesUsed}, distance: ${solution.totalDistance}")
        }
        logger.info("Iterations: $iters")
    }

    private fun iteration(): Boolean {
        val originalDistance = solution.totalDistance

        val routeRemoval = findRouteRemoval()
        val bestSwap = if (routeRemoval.isNotEmpty()) {
            logger.trace("Removed a vehicle")
            routeRemoval.maxOrNull()!!
        } else {
            findImprovements().maxOrNull() ?: return false
        }

        performSwap(bestSwap)

        logger.trace(bestSwap.toString())
        logger.trace("Saved distance: ${bestSwap.distanceSavings}")
        logger.trace(Solution.fromSolutionBuilder(solution).formatOutput())

        val finalDistance = solution.totalDistance
        if (abs(originalDistance - finalDistance - bestSwap.distanceSavings) > 1e-5)
            throw RuntimeException("bad distance - expected ${bestSwap.distanceSavings}, got ${originalDistance - finalDistance}")

        return true
    }

    private fun findRouteRemoval(): List<TwoOptContainer> {
        val routeRemoval = mutableListOf<TwoOptContainer>()
        val routes = solution.routes

        for (routeId1 in 0 until routes.size) {
            for (routeId2 in 0 until routes.size) {
                if (routeId1 == routeId2)
                    continue

                val route1 = routes[routeId1]
                val route2 = routes[routeId2]
                val nodeOrdinal1 = 0
                val nodeOrdinal2 = route2.route.size - 2

                val distanceSavings = twoOptSwapSaving(route1, nodeOrdinal1, route2, nodeOrdinal2, true)
                if (!distanceSavings.isNaN())
                    routeRemoval.add(TwoOptContainer(routeId1, routeId2, nodeOrdinal1, nodeOrdinal2, distanceSavings))
            }
        }

        logger.trace("Valid route removals count: ${routeRemoval.size}")
        return routeRemoval
    }

    private fun findImprovements(): List<TwoOptContainer> {
        val improvements = mutableListOf<TwoOptContainer>()
        val routes = solution.routes

        for (routeId1 in 0 until (routes.size - 1)) {
            for (routeId2 in (routeId1 + 1) until routes.size) {
                val route1 = routes[routeId1]
                val route2 = routes[routeId2]

                for (nodeOrdinal1 in 0 until (route1.route.size - 1)) {
                    for (nodeOrdinal2 in 1 until (route2.route.size - 1)) {
                        val distanceSavings = twoOptSwapSaving(route1, nodeOrdinal1, route2, nodeOrdinal2, false)
                        if (distanceSavings.isNaN())
                            continue

                        improvements.add(
                            TwoOptContainer(routeId1, routeId2, nodeOrdinal1, nodeOrdinal2, distanceSavings)
                        )
                    }
                }
            }
        }

        logger.trace("Valid swaps count: ${improvements.size}")
        return improvements
    }

    private fun performSwap(bestSwap: TwoOptContainer) {
        val routeBuilder1 = solution.routes[bestSwap.routeId1]
        val routeBuilder2 = solution.routes[bestSwap.routeId2]

        val route1SecondPart = routeBuilder1.route.subList(bestSwap.nodeOrdinal1 + 1, routeBuilder1.route.size).toList()
        val route2SecondPart = routeBuilder2.route.subList(bestSwap.nodeOrdinal2 + 1, routeBuilder2.route.size).toList()

        merge(bestSwap.nodeOrdinal1, routeBuilder1, route2SecondPart)
        merge(bestSwap.nodeOrdinal2, routeBuilder2, route1SecondPart)

        if (routeBuilder1.route.size == 2) {
            solution.routes.removeAt(bestSwap.routeId1)
        } else if (routeBuilder2.route.size == 2) {
            solution.routes.removeAt(bestSwap.routeId2)
        }
    }

    private fun merge(nodeOrdinal: Int, routeBuilder: Ant.SolutionBuilder.RouteBuilder, nodesToAdd: List<NodeMeta>) {
        val route = routeBuilder.route

        while (route.size > nodeOrdinal + 1)
            route.removeLast()

        for (nodeMeta in nodesToAdd) {
            val node = nodeMeta.node
            val lastNodeMeta = route.last()
            val arrivalTime = lastNodeMeta.departureTime + travelTime[lastNodeMeta.node.id, node.id]
            val departureTime = maxOf(arrivalTime, node.readyTime) + node.serviceTime
            route.add(NodeMeta(node, arrivalTime, departureTime))
        }

        // routeBuilder.remainingCapacity not updated because it is not used
        routeBuilder.totalDistance = routeBuilder.route.zipWithNext { nm1, nm2 ->
            distances[nm1.node.id, nm2.node.id]
        }.sum()
    }

    private fun calculateDistanceSavings(
        route1: Ant.SolutionBuilder.RouteBuilder, nodeOrdinal1: Int,
        route2: Ant.SolutionBuilder.RouteBuilder, nodeOrdinal2: Int
    ): Double {
        val nodeMeta1 = route1.route[nodeOrdinal1]
        val nodeMeta1Next = route1.route[nodeOrdinal1 + 1]

        val nodeMeta2 = route2.route[nodeOrdinal2]
        val nodeMeta2Next = route2.route[nodeOrdinal2 + 1]

        val currentDistance = distances[nodeMeta1.node.id, nodeMeta1Next.node.id] +
                distances[nodeMeta2.node.id, nodeMeta2Next.node.id]

        val swappedDistance = distances[nodeMeta1.node.id, nodeMeta2Next.node.id] +
                distances[nodeMeta2.node.id, nodeMeta1Next.node.id]

        return currentDistance - swappedDistance
    }

    private fun twoOptSwapSaving(
        route1: SolutionBuilder.RouteBuilder, nodeOrdinal1: Int,
        route2: SolutionBuilder.RouteBuilder, nodeOrdinal2: Int,
        isNonImprovingAllowed: Boolean
    ): Double {
        val nodeMeta1 = route1.route[nodeOrdinal1]
        val nodeMeta2 = route2.route[nodeOrdinal2]

        val distanceSavings = calculateDistanceSavings(route1, nodeOrdinal1, route2, nodeOrdinal2)
        if (!isNonImprovingAllowed && distanceSavings <= 1e-8)
            return Double.NaN

        val route1SecondPart = route1.route.subList(nodeOrdinal1 + 1, route1.route.size)
        val route2SecondPart = route2.route.subList(nodeOrdinal2 + 1, route2.route.size)

        val totalDemand1 = sumDemand(route1.route.subList(0, nodeOrdinal1 + 1), route2SecondPart)
        if (totalDemand1 > instance.capacity)
            return Double.NaN

        val totalDemand2 = sumDemand(route2.route.subList(0, nodeOrdinal2 + 1), route1SecondPart)
        if (totalDemand2 > instance.capacity)
            return Double.NaN

        if (!validateTimeWindows(nodeMeta1, route2SecondPart))
            return Double.NaN
        if (!validateTimeWindows(nodeMeta2, route1SecondPart))
            return Double.NaN

        return distanceSavings
    }

    companion object {
        data class TwoOptContainer(
            val routeId1: Int,
            val routeId2: Int,
            val nodeOrdinal1: Int,
            val nodeOrdinal2: Int,
            val distanceSavings: Double
        ) : Comparable<TwoOptContainer> {
            override fun compareTo(other: TwoOptContainer) = this.distanceSavings.compareTo(other.distanceSavings)
        }

        private fun sumDemand(routePart1: List<NodeMeta>, routePart2: List<NodeMeta>) =
            routePart1.sumOf { it.node.demand } + routePart2.sumOf { it.node.demand }

        private fun validateTimeWindows(startingNodeMeta: NodeMeta, routePart: List<NodeMeta>): Boolean {
            var previousNodeId = startingNodeMeta.node.id
            var previousDepartureTime = startingNodeMeta.departureTime
            for (nodeMeta in routePart) {
                val node = nodeMeta.node

                val arrivalTime = previousDepartureTime + travelTime[previousNodeId, node.id]
                if (arrivalTime > node.dueTime)
                    return false
                if (arrivalTime <= node.serviceTime)
                    return true  // terminate early because we know the rest of the route must be valid

                previousNodeId = node.id
                previousDepartureTime = maxOf(arrivalTime, node.readyTime) + node.serviceTime
            }
            return true
        }
    }
}
