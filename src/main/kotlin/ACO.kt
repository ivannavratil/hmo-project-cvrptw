import aco.AntColony
import com.sksamuel.hoplite.ConfigLoader
import helpers.Config
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import shared.Instance
import shared.Solution
import java.time.LocalDateTime

// TODO Remove all F64Array if calculations are not vectorized.
// TODO Array / ArrayList / List ?
// TODO Sparse structures?

fun main() {
    val logger: Logger = LogManager.getLogger("main")
    Configurator.setRootLevel(Level.TRACE)

    val config = try {
        ConfigLoader().loadConfigOrThrow("/config.yaml")
    } catch (ex: Exception) {
        logger.warn("Using values set programmatically!")
        Config(
            1, 100,
            Config.Ant(69, 1.0, 2.0, 3.0, 0.75, 0.75, 0.1),
            Config.AntColony(
                tauZero = 0.001,
                Config.AntColony.SimulatedAnnealing(
                    startingTemperature = 100.0,
                    decrementFunction = 1,
                    decrementParameter = 0.99,
                    solutionAcceptance = 0,
                    terminationCriteria = 0,
                    terminationFinalTemperature = 0.01
                )
            )
        )
    }
    println("helpers.Config setup: $config")

    val instance = Instance.fromInstanceId(config.instanceId)

    val aco = AntColony(instance, config.antColony)

    aco.run(config)

    val timeStamp = LocalDateTime.now()

    Solution.fromSolutionBuilder(aco.incumbentSolution!!)
        .exportToFile("src/main/resources/results/i${config.instanceId}-$timeStamp.txt")

    //File("src/main/resources/results/i${config.instanceId}-$timeStamp.json").writeText(Json.encodeToString(config))

    //deserialization
    //Json.decodeFromString<Config>(File("src/main/resources/results/i${config.instanceId}-$timeStamp.json").readLines().first())
}
