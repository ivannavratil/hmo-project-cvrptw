package local

import org.apache.logging.log4j.LogManager
import shared.Instance
import shared.NodeMeta
import shared.RouteBuilder
import shared.SolutionBuilder
import kotlin.math.abs

private interface ISwap : Comparable<ISwap> {
    val distanceSavings: Double
    fun performSwap(solution: SolutionBuilder)
    override fun compareTo(other: ISwap) = distanceSavings.compareTo(other.distanceSavings)
}

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
//            logger.info("Found new best solution - vehicles: ${solution.vehiclesUsed}, distance: ${solution.totalDistance}")
        }
//        logger.info("Iterations: $iters")
    }

    private fun iteration(): Boolean {
        val originalDistance = solution.totalDistance

        var bestSwap: ISwap?
        val routeRemoval = findRouteRemoval()
        if (routeRemoval.isNotEmpty()) {
            logger.trace("Removed a vehicle")
            bestSwap = routeRemoval.maxOrNull()!!
        } else {
            bestSwap = findTwoOptImprovements().maxOrNull()

            //////

//            val step = if (seededRandom.nextBoolean()) 1 else 2
//            val otherStep = if (step == 1) 2 else 1
//            if (bestSwap == null) bestSwap = findInternalSwapImprovements(step).maxOrNull()
//            if (bestSwap == null) bestSwap = findInternalSwapImprovements(otherStep).maxOrNull()

            //////

            // TODO gives best results for a single i6 solution
            if (bestSwap == null) {
                bestSwap = findInternalSwapImprovements(1).maxOrNull()
                val bestSwap2 = findInternalSwapImprovements(2).maxOrNull()
                if (bestSwap == null || bestSwap2 != null && bestSwap2 > bestSwap)
                    bestSwap = bestSwap2
            }

            //////

            // TODO try taking best of all types - not that good tbh
//            val bestSwap1 = findInternalSwapImprovements(1).maxOrNull()
//            if (bestSwap == null || bestSwap1 != null && bestSwap1 > bestSwap)
//                bestSwap = bestSwap1
//            val bestSwap2 = findInternalSwapImprovements(2).maxOrNull()
//            if (bestSwap == null || bestSwap2 != null && bestSwap2 > bestSwap)
//                bestSwap = bestSwap2
        }

        bestSwap?.performSwap(solution) ?: return false

//        logger.trace(bestSwap.toString())
//        logger.trace("Saved distance: ${bestSwap.distanceSavings}")
//        logger.trace(Solution.fromSolutionBuilder(solution).formatOutput())

        val finalDistance = solution.totalDistance
        if (abs(originalDistance - finalDistance - bestSwap.distanceSavings) > 1e-5)
            throw RuntimeException("bad distance - expected ${bestSwap.distanceSavings}, got ${originalDistance - finalDistance}")

        return true
    }

    private fun findInternalSwapImprovements(step: Int): List<InternalSwapContainer> {
        val internalSwaps = mutableListOf<InternalSwapContainer>()
        val routes = solution.routes

        for (routeId in 0 until routes.size) {
            val route = routes[routeId].route
            for (nodeOrdinal in 1 until (route.size - step - 1)) {
                val distanceSavings = internalSwapSaving(route, nodeOrdinal, step)
                if (!distanceSavings.isNaN()) {
                    internalSwaps.add(InternalSwapContainer(routeId, nodeOrdinal, step, distanceSavings))
                }
            }
        }

        return internalSwaps
    }

    private fun internalSwapSaving(route: MutableList<NodeMeta>, nodeOrdinal: Int, step: Int): Double {
        // swapping nm1 and nm2 or nm1 and nm3
        val nmPrev = route[nodeOrdinal - 1]
        val nm1 = route[nodeOrdinal]
        val nm2 = route[nodeOrdinal + 1]
        val nm3 = route[nodeOrdinal + 2]
        val nmLast = route[nodeOrdinal + step]
        val nmNext = route[nodeOrdinal + step + 1]

        val distanceSavings = calculateDistanceSavings(
            node1Id = nmPrev.node.id,
            node1IdNext = nm1.node.id,
            node2Id = nmNext.node.id,
            node2IdNext = nmLast.node.id
        )
        if (distanceSavings <= 0)
            return Double.NaN

        // Demand will not be an issue.

        // TODO optimize?
        val swappingPart = if (step == 1) arrayListOf(nm2, nm1, nm3) else arrayListOf(nm3, nm2, nm1)
        val routeSecondPart = swappingPart + route.subList(nodeOrdinal + 3, route.size)
        if (!validateTimeWindows(nmPrev, routeSecondPart))
            return Double.NaN

        return distanceSavings
    }

    // TODO possible to remove car by injecting a route with a single customer?
    private fun findRouteRemoval(): List<TwoOptSwapContainer> {
        val routeRemoval = mutableListOf<TwoOptSwapContainer>()
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
                if (!distanceSavings.isNaN()) {
                    routeRemoval.add(
                        TwoOptSwapContainer(routeId1, routeId2, nodeOrdinal1, nodeOrdinal2, distanceSavings)
                    )
                }
            }
        }

//        logger.trace("Valid route removals count: ${routeRemoval.size}")
        return routeRemoval
    }

    private fun findTwoOptImprovements(): List<TwoOptSwapContainer> {
        val improvements = mutableListOf<TwoOptSwapContainer>()
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
                            TwoOptSwapContainer(routeId1, routeId2, nodeOrdinal1, nodeOrdinal2, distanceSavings)
                        )
                    }
                }
            }
        }

//        logger.trace("Valid swaps count: ${improvements.size}")
        return improvements
    }

    private fun merge(nodeOrdinal: Int, routeBuilder: RouteBuilder, nodesToAdd: List<NodeMeta>) {
        val route = routeBuilder.route

        while (route.size > nodeOrdinal + 1)
            route.removeLast()

        for (nodeMeta in nodesToAdd)
            route.add(route.last().calculateNext(nodeMeta.node, instance))

        updateTotalDistance(routeBuilder)
        updateRemainingCapacity(routeBuilder)
    }

    private fun calculateTwoOptSwapDistanceSavings(
        route1: RouteBuilder, nodeOrdinal1: Int,
        route2: RouteBuilder, nodeOrdinal2: Int
    ) = calculateDistanceSavings(
        node1Id = route1.route[nodeOrdinal1].node.id,
        node1IdNext = route1.route[nodeOrdinal1 + 1].node.id,
        node2Id = route2.route[nodeOrdinal2].node.id,
        node2IdNext = route2.route[nodeOrdinal2 + 1].node.id
    )

    private fun twoOptSwapSaving(
        route1: RouteBuilder, nodeOrdinal1: Int,
        route2: RouteBuilder, nodeOrdinal2: Int,
        isNonImprovingAllowed: Boolean
    ): Double {
        val nodeMeta1 = route1.route[nodeOrdinal1]
        val nodeMeta2 = route2.route[nodeOrdinal2]

        val distanceSavings = calculateTwoOptSwapDistanceSavings(route1, nodeOrdinal1, route2, nodeOrdinal2)
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

    private fun calculateDistanceSavings(node1Id: Int, node1IdNext: Int, node2Id: Int, node2IdNext: Int): Double {
        val distances = instance.distances
        val currentDistance = distances[node1Id, node1IdNext] + distances[node2Id, node2IdNext]
        val swappedDistance = distances[node1Id, node2IdNext] + distances[node2Id, node1IdNext]
        return currentDistance - swappedDistance
    }

    private fun updateTotalDistance(routeBuilder: RouteBuilder) {
        routeBuilder.totalDistance = routeBuilder.route.zipWithNext { nm1, nm2 ->
            instance.distances[nm1.node.id, nm2.node.id]
        }.sum()
    }

    private fun updateRemainingCapacity(routeBuilder: RouteBuilder) {
        routeBuilder.remainingCapacity = instance.capacity - routeBuilder.route.sumOf { it.node.demand }
    }

    private fun sumDemand(routePart1: List<NodeMeta>, routePart2: List<NodeMeta>): Int {
        return routePart1.sumOf { it.node.demand } + routePart2.sumOf { it.node.demand }
    }

    private fun validateTimeWindows(startingNodeMeta: NodeMeta, routePart: List<NodeMeta>): Boolean {
        var previousNodeId = startingNodeMeta.node.id
        var previousDepartureTime = startingNodeMeta.departureTime
        for (nodeMeta in routePart) {
            val node = nodeMeta.node

            val arrivalTime = previousDepartureTime + instance.travelTime[previousNodeId, node.id]
            if (arrivalTime > node.dueTime)
                return false

            previousNodeId = node.id
            previousDepartureTime = maxOf(arrivalTime, node.readyTime) + node.serviceTime
        }
        return true
    }

    private inner class InternalSwapContainer(
        val routeId: Int,
        val nodeOrdinal: Int,
        step: Int,
        override val distanceSavings: Double
    ) : ISwap {
        val step: Int

        init {
            if (step != 1 && step != 2)
                throw IllegalArgumentException("bad step value: $step")
            this.step = step
        }

        override fun performSwap(solution: SolutionBuilder) {
            val routeBuilder = solution.routes[routeId]
            val route = routeBuilder.route

            val tmp = route[nodeOrdinal]
            route[nodeOrdinal] = route[nodeOrdinal + step]
            route[nodeOrdinal + step] = tmp

            for (i in nodeOrdinal until route.size) {
                route[i] = route[i - 1].calculateNext(route[i].node, instance)
            }

            // remainingCapacity is unchanged
            updateTotalDistance(routeBuilder)
        }
    }

    private inner class TwoOptSwapContainer(
        val routeId1: Int,
        val routeId2: Int,
        val nodeOrdinal1: Int,
        val nodeOrdinal2: Int,
        override val distanceSavings: Double
    ) : ISwap {
        private fun copySecondPart(routeBuilder: RouteBuilder, nodeOrdinal: Int) =
            routeBuilder.route.subList(nodeOrdinal + 1, routeBuilder.route.size).toList()

        override fun performSwap(solution: SolutionBuilder) {
            val routeBuilder1 = solution.routes[routeId1]
            val routeBuilder2 = solution.routes[routeId2]

            val route1SecondPart = copySecondPart(routeBuilder1, nodeOrdinal1)
            val route2SecondPart = copySecondPart(routeBuilder2, nodeOrdinal2)
            // Do not reorder copy and merge.
            merge(nodeOrdinal1, routeBuilder1, route2SecondPart)
            merge(nodeOrdinal2, routeBuilder2, route1SecondPart)

            if (routeBuilder1.route.size == 2) {
                solution.routes.removeAt(routeId1)
            } else if (routeBuilder2.route.size == 2) {
                solution.routes.removeAt(routeId2)
            }
        }
    }
}
