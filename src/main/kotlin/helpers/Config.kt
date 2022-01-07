package helpers

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Duration

// Total maximum runtime is equal to antColony.runtime + finalLocalSearch.runtime

@Serializable
data class Config(
    val ant: Ant,
    val antColony: AntColony,
    val finalLocalSearch: LocalSearch
) {
    fun deepCopy(): Config = this.copy(
        ant = this.ant.copy(),
        antColony = this.antColony.copy(),
        finalLocalSearch = this.finalLocalSearch.copy()
    )

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
        val estimateTauZero: Boolean,
        val estimateLocalSearch: LocalSearch?  // counted within AntColony runtime
    ) {
        override fun toString(): String {
            return "AntColony(iterations=$iterations, runtime=${runtime.toSeconds()}s, tauZero=$tauZero, " +
                    "estimateTauZero=$estimateTauZero, estimateLocalSearch=$estimateLocalSearch)"
        }
    }

    @Serializable
    data class LocalSearch(
        val iterations: Int,
        @Contextual
        val runtime: Duration
    ) {
        override fun toString(): String {
            return "LocalSearch(iterations=$iterations, runtime=${runtime.toSeconds()}s)"
        }
    }
}
