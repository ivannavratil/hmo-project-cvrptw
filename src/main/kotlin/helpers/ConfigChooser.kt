package helpers

import java.time.Duration

object ConfigChooser {

    fun getConfig(instanceId: Int, runtime: Duration = Duration.ofSeconds(120)): Config {
        return when (instanceId) {
            1 -> Config(
                1,
                Config.Ant(80, 0.7, 1.2, 0.55, 0.2, 0.08),
                Config.AntColony(Int.MAX_VALUE, runtime, 1E-3, true)
            )

            2 -> Config(
                2,
                Config.Ant(80, 1.0, 1.25, 0.45, 0.4, 0.2),
                Config.AntColony(Int.MAX_VALUE, runtime, 2E-4, true)
            )

            3 -> Config(
                3,
                Config.Ant(20, 1.0, 1.4, 0.5, 0.4, 0.2),
                Config.AntColony(Int.MAX_VALUE, runtime, 7E-5, true)
            )

            4 -> Config(
                4,
                Config.Ant(20, 1.1, 1.25, 0.7, 0.4, 0.15),
                Config.AntColony(Int.MAX_VALUE, runtime, 6E-5, true)
            )

            5 -> Config(
                5,
                Config.Ant(14, 1.25, 1.2, 0.72, 0.4, 0.15),
                Config.AntColony(Int.MAX_VALUE, runtime, 2E-5, true)
            )

            6 -> Config(
                6,
                Config.Ant(6, 0.9, 1.25, 0.6, 0.4, 0.2),
                Config.AntColony(Int.MAX_VALUE, runtime, 1E-6, true)
            )

            else -> throw RuntimeException("bad instance ID")
        }
    }

}
