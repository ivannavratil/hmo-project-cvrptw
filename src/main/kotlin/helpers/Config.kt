package helpers

data class Config(
    val instanceId: Int,
    val iterations: Int,
    val ant: Ant,
    val antColony: AntColony
) {
    data class Ant(
        var count: Int,
        var alpha: Double,
        var beta: Double,
        var lambda: Double,
        var theta: Double,
        var q0: Double,
        var rho: Double
    )

    data class AntColony(
        val tauZero: Double,
        val startingTemperature: Double
    )
}
