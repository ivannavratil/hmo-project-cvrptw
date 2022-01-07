package local

import helpers.*
import org.apache.logging.log4j.LogManager
import shared.Instance
import shared.NodeMeta
import shared.RouteBuilder
import shared.SolutionBuilder
import java.time.Duration
import java.time.Instant
import kotlin.math.abs
import kotlin.math.ceil

private interface ISwap : Comparable<ISwap> {
    val distanceSavings: Double
    fun performSwap(solution: SolutionBuilder)
    override fun compareTo(other: ISwap) = distanceSavings.compareTo(other.distanceSavings)
}

class LocalSearch(
    private val instance: Instance,
    private val originalSolution: SolutionBuilder  // remains unaltered
) {
    private lateinit var currentSolution: SolutionBuilder
    var incumbentSolution = originalSolution
        private set

    var evaluations = 0
        private set
    var incumbentEvaluations = 0
        private set
    var incumbentTime: Duration = Duration.ZERO
        private set

    private val randomChoosers = arrayListOf(::chooseBestSwap3Random, ::chooseBestSwap4Random, ::chooseBestSwap5Random)
    private val randomChoosersLottery = WeightedLottery(doubleArrayOf(0.3, 0.5, 0.2))

    private lateinit var startTime: Instant
    private val logger = LogManager.getLogger(this::class.java.simpleName)

    fun quickSearch(config: Config.LocalSearch): SolutionBuilder {
        startTime = Instant.now()
        val compositeTermination = CompositeTermination(
            TotalTimeTermination(startTime + config.runtime),
            TotalIterationsTermination(config.iterations)
        )
        searchInternal(compositeTermination, ::chooseBestSwap1)
        return incumbentSolution
    }

    // TODO Track which chooser found incumbent solution?
    fun fullSearch(config: Config.LocalSearch): SolutionBuilder {
        startTime = Instant.now()
        val timeTermination = TotalTimeTermination(startTime + config.runtime)
        val compositeTermination = CompositeTermination(
            timeTermination, TotalIterationsTermination(config.iterations)
        )

        // Try in order of quality and predictability, to make the most use of limited runtime.
        searchInternal(compositeTermination, ::chooseBestSwap1)
        searchInternal(compositeTermination, ::chooseBestSwap2)

        while (!timeTermination.terminate()) {
            searchInternal(compositeTermination, randomChoosers[randomChoosersLottery.draw()])
        }

        return incumbentSolution
    }

    private fun searchInternal(terminationCriteria: ITerminationCriteria, chooser: () -> ISwap?) {
        currentSolution = originalSolution.deepCopy()
        var iters = 0
        while (!terminationCriteria.terminate(iters++)) {
            if (!iteration(chooser))
                break
            // currentSolution was improved

            if (currentSolution !== incumbentSolution && currentSolution >= incumbentSolution)
                continue

            incumbentSolution = currentSolution
            incumbentEvaluations = evaluations
            incumbentTime = Duration.between(startTime, Instant.now())
//            logger.info(
//                "Found new best solution - " +
//                        "vehicles: ${incumbentSolution.vehiclesUsed}, distance: ${incumbentSolution.totalDistance}, " +
//                        "time: ${incumbentTime.toSeconds()}s, evaluations: $incumbentEvaluations"
//            )
        }
        logger.info("Iterations: $iters")
    }

    private fun chooseBetter(current: ISwap?, new: ISwap?): ISwap? =
        if (current == null || new != null && new > current) new else current

    private fun chooseBestSwap1(): ISwap? {
        // Prefer 2-opt and transfer. Slower but usually better.
        var bestSwap = findTwoOptImprovements().maxOrNull() as ISwap?
        if (bestSwap == null) {
            bestSwap = chooseBetter(
                findInternalSwapImprovements(1).maxOrNull(),
                findInternalSwapImprovements(2).maxOrNull()
            )
        }
        return chooseBetter(bestSwap, findNodeTransferImprovements().maxOrNull())
    }

    private fun chooseBestSwap2(): ISwap? {
        // Choose the greediest option. Fast but usually worse.
        var bestSwap = findTwoOptImprovements().maxOrNull() as ISwap?
        bestSwap = chooseBetter(bestSwap, findInternalSwapImprovements(1).maxOrNull())
        bestSwap = chooseBetter(bestSwap, findInternalSwapImprovements(2).maxOrNull())
        bestSwap = chooseBetter(bestSwap, findNodeTransferImprovements().maxOrNull())
        return bestSwap
    }

    private fun chooseBestSwap3Random(): ISwap? {
        // Randomly choose a neighborhood and then the best swap from it.
        return listOf(
            { findTwoOptImprovements() },
            { findInternalSwapImprovements(1) },
            { findInternalSwapImprovements(2) },
            { findNodeTransferImprovements() }
        ).shuffled(seededRandom)
            .asSequence()
            .map { it -> it().maxOrNull() }
            .firstNotNullOfOrNull { it }
    }

    private fun chooseBestSwap4Random(): ISwap? {
        // Choose completely randomly from all neighborhoods.
        return generateAllNeighbors().randomOrNull(seededRandom)
    }

    private fun chooseBestSwap5Random(): ISwap? {
        // Choose from a RCL (best 20%).
        val allSwaps = generateAllNeighbors()
        if (allSwaps.isEmpty())
            return null
        allSwaps.sortByDescending { it }
        return allSwaps[seededRandom.nextInt(ceil(allSwaps.size * 0.2).toInt())]
    }

    private fun generateAllNeighbors(): ArrayList<ISwap> {
        val allSwaps = ArrayList<ISwap>()
        findTwoOptImprovements().filterNotNullTo(allSwaps)
        findInternalSwapImprovements(1).filterNotNullTo(allSwaps)
        findInternalSwapImprovements(2).filterNotNullTo(allSwaps)
        findNodeTransferImprovements().filterNotNullTo(allSwaps)
        return allSwaps
    }

    private fun iteration(chooser: () -> ISwap?): Boolean {
        evaluations++
        val originalDistance = currentSolution.totalDistance

        val routeRemovals = findTwoOptRouteRemoval() + findNodeTransferImprovements(true)
        val bestSwap = if (routeRemovals.isNotEmpty()) {
            logger.trace("Removed a vehicle via LS!!!")
            routeRemovals.maxOrNull()!!
        } else {
            chooser()
        }

        bestSwap?.performSwap(currentSolution) ?: return false

//        logger.trace(bestSwap.toString())
//        logger.trace("Saved distance: ${bestSwap.distanceSavings}")
//        logger.trace(Solution.fromSolutionBuilder(currentSolution).formatOutput())

        val finalDistance = currentSolution.totalDistance
        if (abs(originalDistance - finalDistance - bestSwap.distanceSavings) > 1e-5)
            throw RuntimeException("bad distance - expected ${bestSwap.distanceSavings}, got ${originalDistance - finalDistance}")

        return true
    }

    private fun findInternalSwapImprovements(step: Int): List<InternalSwapContainer> {
        val internalSwaps = mutableListOf<InternalSwapContainer>()
        val routes = currentSolution.routes

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

        // Demand will always be satisfied.

        if (step == 1) {
            route[nodeOrdinal] = nm2
            route[nodeOrdinal + 1] = nm1
        } else {
            route[nodeOrdinal] = nm3
            route[nodeOrdinal + 1] = nm2
            route[nodeOrdinal + 2] = nm1
        }
        val isValid = validateTimeWindows(nmPrev, route.subList(nodeOrdinal, route.size))
        route[nodeOrdinal] = nm1
        route[nodeOrdinal + 1] = nm2
        route[nodeOrdinal + 2] = nm3

        if (!isValid)
            return Double.NaN

        return distanceSavings
    }

    private fun findTwoOptRouteRemoval(): List<TwoOptSwapContainer> {
        val routeRemoval = mutableListOf<TwoOptSwapContainer>()
        val routes = currentSolution.routes

        for (routeId1 in 0 until routes.size) {
            val route1 = routes[routeId1]

            for (routeId2 in 0 until routes.size) {
                if (routeId1 == routeId2)
                    continue

                val route2 = routes[routeId2]
                val nodeOrdinal1 = 0
                val nodeOrdinal2 = route2.route.size - 2

                val distanceSavings = twoOptSwapSaving(route1, nodeOrdinal1, route2, nodeOrdinal2, false)
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
        val routes = currentSolution.routes

        for (routeId1 in 0 until (routes.size - 1)) {
            val route1 = routes[routeId1]

            for (routeId2 in (routeId1 + 1) until routes.size) {
                val route2 = routes[routeId2]

                for (nodeOrdinal1 in 0 until (route1.route.size - 1)) {
                    for (nodeOrdinal2 in 1 until (route2.route.size - 1)) {
                        val distanceSavings = twoOptSwapSaving(route1, nodeOrdinal1, route2, nodeOrdinal2, true)
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

        updateDistanceAndCapacity(routeBuilder)
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
        onlyImproving: Boolean
    ): Double {
        val nodeMeta1 = route1.route[nodeOrdinal1]
        val nodeMeta2 = route2.route[nodeOrdinal2]

        val distanceSavings = calculateTwoOptSwapDistanceSavings(route1, nodeOrdinal1, route2, nodeOrdinal2)
        if (onlyImproving && distanceSavings <= 1e-8)
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

    private fun findNodeTransferImprovements(onlyRouteRemoval: Boolean = false): List<NodeTransferContainer> {
        val improvements = mutableListOf<NodeTransferContainer>()
        val routes = currentSolution.routes

        for (routeId1 in 0 until routes.size) {
            val route1 = routes[routeId1]

            if (onlyRouteRemoval && route1.route.size != 3)
                continue

            for (routeId2 in 0 until routes.size) {
                if (routeId1 == routeId2)
                    continue

                val route2 = routes[routeId2]

                for (nodeOrdinal1 in 1 until (route1.route.size - 1)) {
                    val demand = route1.route[nodeOrdinal1].node.demand
                    if (route2.remainingCapacity < demand)
                        continue

                    for (nodeOrdinal2 in 1 until route2.route.size) {
                        val distanceSavings = nodeTransferSaving(
                            route1, nodeOrdinal1, route2, nodeOrdinal2, !onlyRouteRemoval
                        )
                        if (!distanceSavings.isNaN()) {
                            improvements.add(
                                NodeTransferContainer(routeId1, routeId2, nodeOrdinal1, nodeOrdinal2, distanceSavings)
                            )
                        }
                    }
                }
            }
        }

        return improvements
    }

    private fun nodeTransferSaving(
        route1: RouteBuilder, nodeOrdinal1: Int,
        route2: RouteBuilder, nodeOrdinal2: Int,
        onlyImproving: Boolean
    ): Double {
        // We know route2 has enough capacity to accept the node.
        val route1Raw = route1.route
        val route2Raw = route2.route

        val nodeMeta2 = route1Raw[nodeOrdinal1]
        val nodeMeta4 = route2Raw[nodeOrdinal2 - 1]
        val n1 = route1Raw[nodeOrdinal1 - 1].node.id
        val n2 = nodeMeta2.node.id
        val n3 = route1Raw[nodeOrdinal1 + 1].node.id
        val n4 = nodeMeta4.node.id
        val n5 = route2Raw[nodeOrdinal2].node.id

        val distances = instance.distances
        val currentDistance = distances[n1, n2] + distances[n2, n3] + distances[n4, n5]
        val swappedDistance = distances[n1, n3] + distances[n4, n2] + distances[n2, n5]

        val distanceSavings = currentDistance - swappedDistance
        if (onlyImproving && distanceSavings <= 0)
            return Double.NaN

        // Because of the triangle inequality, route1 must already have valid time windows.
        // Avoid creating new lists.
        route2Raw[nodeOrdinal2 - 1] = nodeMeta2
        val isValid = validateTimeWindows(nodeMeta4, route2Raw.subList(nodeOrdinal2 - 1, route2Raw.size))
        route2Raw[nodeOrdinal2 - 1] = nodeMeta4
        if (!isValid)
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

    private fun updateDistanceAndCapacity(routeBuilder: RouteBuilder) {
        updateTotalDistance(routeBuilder)
        updateRemainingCapacity(routeBuilder)
    }

    private fun updateNodeMetas(route: MutableList<NodeMeta>, firstBadPos: Int) {
        for (i in firstBadPos until route.size) {
            route[i] = route[i - 1].calculateNext(route[i].node, instance)
        }
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

            updateNodeMetas(route, nodeOrdinal)

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

    private inner class NodeTransferContainer(
        val routeId1: Int,  // removed from here
        val routeId2: Int,  // transferred to here
        val nodeOrdinal1: Int,
        val nodeOrdinal2: Int,
        override val distanceSavings: Double
    ) : ISwap {
        override fun performSwap(solution: SolutionBuilder) {
            val routeBuilder1 = solution.routes[routeId1]
            val routeBuilder2 = solution.routes[routeId2]

            val route1 = routeBuilder1.route
            val transferredNodeMeta = route1[nodeOrdinal1]

            route1.removeAt(nodeOrdinal1)
            updateNodeMetas(route1, nodeOrdinal1)
            if (route1.size == 2) {
                solution.routes.removeAt(routeId1)
            }

            routeBuilder2.route.add(nodeOrdinal2, transferredNodeMeta)
            updateNodeMetas(routeBuilder2.route, nodeOrdinal2)

            updateDistanceAndCapacity(routeBuilder1)
            updateDistanceAndCapacity(routeBuilder2)
        }
    }
}
