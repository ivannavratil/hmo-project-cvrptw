import Distances.distances
import kotlin.math.abs

class SavingsHeuristic(
    private val f: Double = 2.0,
    private val g: Double = 2.0
) {
    fun calculateSavings(i: Int, j: Int): Double {
        return distances[i, 0] + distances[0, j] - g * distances[i, j] + f * abs(distances[i, 0] - distances[0, j])
    }
}
