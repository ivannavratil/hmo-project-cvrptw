data class Config(val instance: Int, val iterations: Int, val ant: Ant, val antColony: AntColony) {
    data class Ant(
        val count: Int,
        val alpha: Double,
        val beta: Double,
        val lambda: Double,
        val theta: Double,
        val q0: Double,
        val rho: Double
    )

    data class AntColony(
        val tauZero: Double,
        val startingTemperature: Double
    )
}