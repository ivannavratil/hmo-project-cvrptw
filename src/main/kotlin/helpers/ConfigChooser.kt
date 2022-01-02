package helpers

object ConfigChooser {

    fun getConfig(instanceId: Int): Config {
        return when (instanceId) {
            1 -> Config(
                1, Int.MAX_VALUE,
                Config.Ant(80, 0.7, 1.2, 3.0, 0.55, 0.2, 0.08),
                Config.AntColony(tauZero = 1E-3)
            )

            2 -> Config(
                2, Int.MAX_VALUE,
                Config.Ant(80, 1.0, 1.25, 3.0, 0.45, 0.4, 0.2),
                Config.AntColony(tauZero = 2E-4)
            )

            3 -> Config(
                3, Int.MAX_VALUE,
                Config.Ant(20, 1.0, 1.4, 3.0, 0.5, 0.4, 0.2),
                Config.AntColony(tauZero = 7E-5)
            )

            4 -> Config(
                4, Int.MAX_VALUE,
                Config.Ant(20, 1.1, 1.25, 3.0, 0.7, 0.4, 0.15),
                Config.AntColony(tauZero = 6E-5)
            )

            5 -> Config(
                5, Int.MAX_VALUE,
                Config.Ant(14, 1.25, 1.2, 3.0, 0.72, 0.4, 0.15),
                Config.AntColony(tauZero = 2E-5)
            )

            6 -> Config(
                6, Int.MAX_VALUE,
                Config.Ant(6, 0.9, 1.25, 0.03, 0.6, 0.4, 0.2),
                Config.AntColony(tauZero = 1E-6)
            )

            else -> throw RuntimeException("bad instance ID")
        }
    }

}