import java.io.File

data class Solution(
    val routes: List<Route>,
    val distance: Double
) {
    companion object {
        fun fromFile(file: File): Solution {
            val buffer = file.readLines()
            val routes = IntRange(1, buffer.size - 2).map { Route.parseRoute(buffer[it]) }.toList()
            val distance = buffer.last().toDouble()
            return Solution(routes, distance)
        }
    }
}
