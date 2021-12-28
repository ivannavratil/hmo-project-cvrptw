package sa

import kotlin.math.log

interface TerminationCriteriaInterface {
    fun terminate(currentTemperature: Double): Boolean
}

// TODO Add termination for N iterations without improvement and for total runtime

class FinalTemperatureTermination(private val finalTemperature: Double) : TerminationCriteriaInterface {
    override fun terminate(currentTemperature: Double): Boolean {
        return currentTemperature < finalTemperature
    }
}

class GeometricDecrementTermination(
    startTemperature: Double,
    finalTemperature: Double,
    alpha: Double
) : TerminationCriteriaInterface {
    private val maxIterations = log(finalTemperature, alpha) - log(startTemperature, alpha)
    private var iterations = 0

    override fun terminate(currentTemperature: Double): Boolean {
        iterations++
        return iterations >= maxIterations
    }
}

class VerySlowDecreaseTermination(
    startTemperature: Double,
    finalTemperature: Double,
    beta: Double
) : TerminationCriteriaInterface {
    private val maxIterations = (startTemperature - finalTemperature) / (beta * startTemperature * finalTemperature)
    private var iterations = 0

    override fun terminate(currentTemperature: Double): Boolean {
        iterations++
        return iterations >= maxIterations
    }
}
