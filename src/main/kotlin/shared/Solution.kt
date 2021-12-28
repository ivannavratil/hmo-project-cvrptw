package shared

import aco.Ant
import java.io.File

data class Solution(
    val routes: List<Route>,
    val distance: Double
) {
    fun formatOutput(): String {
        val builder = StringBuilder()
        builder.append(routes.size).append(System.lineSeparator())
        routes.forEachIndexed { i, route ->
            builder.append("${i + 1}: ${route.formatOutput()}").append(System.lineSeparator())
        }
        builder.append(distance).append(System.lineSeparator())
        return builder.toString()
    }

    fun exportToFile(path: String) {
        File(path).writeText(formatOutput(), Charsets.UTF_8)
    }

    companion object {
        fun fromFile(file: File): Solution {
            val buffer = file.readLines()
            val routes = IntRange(1, buffer.size - 2).map { Route.parseRoute(buffer[it]) }
            val distance = buffer.last().toDouble()
            return Solution(routes, distance)
        }

        fun fromSolutionBuilder(builder: Ant.SolutionBuilder): Solution {
            return Solution(
                builder.routes.map { Route.fromRouteBuilder(it) },
                builder.totalDistance
            )
        }
    }
}
