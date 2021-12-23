data class Route(
    val nodes: List<Pair<Int, Int>>
) {
    companion object {
        fun parseRoute(line: String): Route {
            val nodes = line.split(':')[1].trim().split("->").map {
                val (tmpFirst, tmpSecond) = it.split('(')
                Pair(tmpFirst.toInt(), tmpSecond.trimEnd(')').toInt())
            }
            return Route(nodes)
        }
    }
}
