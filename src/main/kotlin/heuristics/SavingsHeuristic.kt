package heuristics

import helpers.Distances.distances
import kotlin.math.abs

object SavingsHeuristic {
    var f: Double = 2.0  // TODO set values
    var g: Double = 2.0

    fun calculateSavings(i: Int, j: Int): Double {
        return distances[i, 0] + distances[0, j] - g * distances[i, j] + f * abs(distances[i, 0] - distances[0, j])
    }
}
