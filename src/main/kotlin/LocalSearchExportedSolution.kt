import local.LocalSearch
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import shared.Instance
import shared.Solution
import shared.SolutionBuilder
import java.io.File
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

    val startTime = System.currentTimeMillis()
    val iterations = LocalSearch(instance, solutionBuilder).search()
    val runtime = System.currentTimeMillis() - startTime
    logger.info("RUNTIME LOCAL SEARCH: $runtime ms")

    logger.info("Final:\n" + Solution.fromSolutionBuilder(solutionBuilder).formatOutput())

    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
    Solution.fromSolutionBuilder(solutionBuilder)
        .exportToFile("src/main/resources/results/i$instanceId-LS-${formattedTimestamp}-${Random.nextULong()}.txt")
}
