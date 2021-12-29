// Updated from https://github.com/guyko/weighted-lottery

package helpers

import java.util.*

class WeightedLottery(private val weights: DoubleArray) {
    private val accumulatedWeights = DoubleArray(weights.size)

    init {
        if (weights.isNotEmpty()) {
            accumulatedWeights[0] = weights[0].validate()
            (1 until weights.size).forEach {
                accumulatedWeights[it] = weights[it].validate() + accumulatedWeights[it - 1]
            }
        }
    }

    fun draw(): Int {
        val sumOfWeights = accumulatedWeights.last()
        if (sumOfWeights == 0.0) {
            return drawUniformly()
        }
        val key = seededRandom.nextDouble() * sumOfWeights
        val pos = Arrays.binarySearch(accumulatedWeights, key)
        return when {
            pos < 0 -> -pos - 1
            else -> pos
        }
    }

    private fun Double.validate(): Double {
        if (isNaN() || this < 0.0) {
            throw IllegalArgumentException("$weights contains invalid weight: $this")
        }
        return this
    }

    private fun drawUniformly() = seededRandom.nextInt(accumulatedWeights.size)
}
