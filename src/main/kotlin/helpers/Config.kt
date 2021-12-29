package helpers

import kotlinx.serialization.Serializable

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
        val tauZero: Double
    )
}
