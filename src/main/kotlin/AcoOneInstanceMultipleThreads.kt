import aco.AntColony
import helpers.Config
import helpers.ConfigChooser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import shared.Instance
import shared.Solution
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.random.nextULong

fun main() {
    val instanceId = 6

    val base = ConfigChooser.getConfig5m(instanceId)

    File("src/main/resources/graph/config.json").appendText(
        Json.encodeToString(base) + System.lineSeparator()
    )

    for (i in 0 until 10) {
        thread(name = "instance = $instanceId, thread = $i") {
            main2(instanceId, base.deepCopy())
        }
    }
}

fun main2(instanceId: Int, config: Config) {
    val logger: Logger = LogManager.getLogger("main")
    Configurator.setRootLevel(Level.TRACE)

    println("helpers.Config setup: $config")

    val instance = Instance.fromInstanceId(instanceId)

    val aco = AntColony(instance, config)

    val startTime = Instant.now()
    val incumbentSolution = aco.run()
    val runtime = Duration.between(startTime, Instant.now())
    logger.info("RUNTIME: ${runtime.toSeconds()}s ${runtime.toMillisPart()}ms")

    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))

    Solution.fromSolutionBuilder(incumbentSolution!!)
        .exportToFile("src/main/resources/results/i$instanceId-$formattedTimestamp-${Random.nextULong()}.txt")

    File("src/main/resources/graph/results.txt").appendText(
        "$instanceId;${incumbentSolution.vehiclesUsed};${incumbentSolution.totalDistance}" + System.lineSeparator()
    )
}
