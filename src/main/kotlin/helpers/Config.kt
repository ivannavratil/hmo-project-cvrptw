package helpers

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Duration

@Serializable
data class Config(
    val instanceId: Int,
    val ant: Ant,
    val antColony: AntColony,
) {
    fun deepCopy(): Config = this.copy(ant = this.ant.copy(), antColony = this.antColony.copy())

    @Serializable
    data class Ant(
        var count: Int,
        var alpha: Double,
        var beta: Double,
        var theta: Double,
        var q0: Double,
        var rho: Double
    )

    @Serializable
    data class AntColony(
        val iterations: Int,
        @Contextual
        val runtime: Duration,
        var tauZero: Double,
        val estimateTauZero: Boolean
    ) {
        override fun toString(): String {
            return "AntColony(iterations=$iterations, runtime=${runtime.toSeconds()}s, " +
                    "tauZero=$tauZero, estimateTauZero=$estimateTauZero)"
        }
    }
}
