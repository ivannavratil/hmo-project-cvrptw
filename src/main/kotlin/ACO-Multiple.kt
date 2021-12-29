import aco.AntColony
import helpers.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import shared.Instance
import shared.Solution
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main() {

    val instance = 6

    val base = Config(
        instance, 1000,
        Config.Ant(6, 0.9, 1.25, 3.0, 0.6, 0.4, 0.2),
        Config.AntColony(tauZero = 1E-6)
    )

    File("src/main/resources/graph/config.json").appendText(
        Json.encodeToString(base) + System.lineSeparator()
    )

    for (i in 0 until 10) {
        thread(name = "instance = $instance, thread = $i") {
            main2(base.copy())
        }
    }
}

fun main2(config: Config) {
    val logger: Logger = LogManager.getLogger("main")
    Configurator.setRootLevel(Level.TRACE)

    println("helpers.Config setup: $config")

    val instance = Instance.fromInstanceId(config.instanceId)

    val aco = AntColony(instance, config.antColony)

    val runtime = measureTimeMillis {
        aco.run(config)
    }
    logger.info("RUNTIME: $runtime ms")

    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))

    Solution.fromSolutionBuilder(aco.incumbentSolution!!)
        .exportToFile("src/main/resources/results/i${config.instanceId}-${formattedTimestamp}-${Random.nextLong()}.txt")

    File("src/main/resources/graph/results.txt").appendText(
        "${config.instanceId};${aco.incumbentSolution!!.vehiclesUsed};${aco.incumbentSolution!!.totalDistance}" + System.lineSeparator()
    )
}
