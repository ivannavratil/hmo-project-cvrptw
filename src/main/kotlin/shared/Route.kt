package shared

data class Route(
    val nodes: List<Pair<Int, Int>>
) {
    fun formatOutput() = nodes.joinToString("->") { "${it.first}(${it.second})" }

    companion object {
        fun parseRoute(line: String): Route {
            val nodes = line.split(':')[1].trim().split("->").map {
                val (tmpFirst, tmpSecond) = it.split('(')
                Pair(tmpFirst.toInt(), tmpSecond.trimEnd(')').toInt())
            }
            return Route(nodes)
        }

        fun fromRouteBuilder(builder: SolutionBuilder.RouteBuilder): Route {
            val nodes = builder.route.map {
                Pair(it.node.id, it.serviceStartTime)
            }
            return Route(nodes)
        }
    }
}
