package helpers

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val instanceId: Int,
    val iterations: Int,
    val ant: Ant,
    val antColony: AntColony,
) {
    fun deepCopy(): Config = this.copy(ant = this.ant.copy(), antColony = this.antColony.copy())

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
        var tauZero: Double,
        val estimateTauZero: Boolean
    )
}
