import local.LocalSearch
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import shared.Instance
import shared.Solution
import shared.SolutionBuilder
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.random.nextULong


const val instanceId = 6

fun main() {
    val logger: Logger = LogManager.getLogger("main")
    Configurator.setRootLevel(Level.TRACE)

    val solution = Solution.fromFile(File("src/main/resources/results/best/i$instanceId.txt"))
    val instance = Instance.fromInstanceId(instanceId)

    val solutionBuilder = SolutionBuilder(instance)

    for (route in solution.routes) {
        solutionBuilder.createNewRoute()
        for (i in 1 until route.nodes.size) {
            solutionBuilder.addNextNode(instance.nodes[route.nodes[i].first])
        }
    }

    logger.info("Before:\n" + Solution.fromSolutionBuilder(solutionBuilder).formatOutput())

    val startTime = Instant.now()
    val bestSolution = LocalSearch(instance, solutionBuilder).fullSearch(2000, Duration.ofSeconds(60))
    val runtime = Duration.between(startTime, Instant.now())
    logger.info("RUNTIME LOCAL SEARCH: ${runtime.toSeconds()}s ${runtime.toMillisPart()}ms")

    logger.info("Final:\n" + Solution.fromSolutionBuilder(bestSolution).formatOutput())

    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
    Solution.fromSolutionBuilder(bestSolution)
        .exportToFile("src/main/resources/results/i$instanceId-LS-${formattedTimestamp}-${Random.nextULong()}.txt")
}
