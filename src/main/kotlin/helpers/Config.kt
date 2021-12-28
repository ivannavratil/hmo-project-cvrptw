package helpers

import kotlinx.serialization.Serializable
import sa.DecrementInterface
import sa.FinalTemperatureTermination
import sa.GeometricDecrement
import sa.GeometricDecrementTermination
import sa.LinearDecrement
import sa.SolutionAcceptanceInterface
import sa.Standard2Acceptance
import sa.StandardAcceptance
import sa.TerminationCriteriaInterface
import sa.VerySlowDecrease
import sa.VerySlowDecreaseTermination

@Serializable
data class Config(
    val instanceId: Int,
    val iterations: Int,
    val ant: Ant,
    val antColony: AntColony,
) {
    @Serializable
    data class Ant(
        var count: Int,
        var alpha: Double,
        var beta: Double,
        var lambda: Double,
        var theta: Double,
        var q0: Double,
        var rho: Double
    )

    @Serializable
    data class AntColony(
        val tauZero: Double,
        val simulatedAnnealing: SimulatedAnnealing
    ) {
        @Serializable
        data class SimulatedAnnealing(
            var startingTemperature: Double,

            var decrementFunction: Int,
            @kotlinx.serialization.Transient
            var decrement: DecrementInterface? = null,
            var decrementParameter: Double,

            var solutionAcceptance: Int,
            @kotlinx.serialization.Transient
            var solution: SolutionAcceptanceInterface? = null,

            var terminationCriteria: Int,
            @kotlinx.serialization.Transient
            var termination: TerminationCriteriaInterface? = null,
            var terminationFinalTemperature: Double? = null,
            var terminationParameter: Double? = null

        ) {
            init {
                decrement = when (decrementFunction) {
                    0 -> LinearDecrement(decrementParameter)
                    1 -> GeometricDecrement(decrementParameter)
                    2 -> VerySlowDecrease(decrementParameter)
                    else -> {
                        throw IllegalArgumentException("Wrong decrement value!")
                    }
                }
                solution = when (solutionAcceptance) {
                    0 -> StandardAcceptance()
                    1 -> Standard2Acceptance()
                    else -> {
                        throw IllegalArgumentException("Wrong acceptance value!")
                    }
                }
                termination = when (terminationCriteria) {
                    0 -> FinalTemperatureTermination(terminationFinalTemperature!!)
                    1 -> GeometricDecrementTermination(
                        startingTemperature,
                        terminationFinalTemperature!!,
                        terminationParameter!!
                    )
                    2 -> VerySlowDecreaseTermination(
                        startingTemperature,
                        terminationFinalTemperature!!,
                        terminationParameter!!
                    )
                    else -> {
                        throw IllegalArgumentException("Wrong termination value!")
                    }
                }
            }
        }
    }
}
