import aco.Ant
import helpers.Config
import helpers.Distances
import helpers.FlatSquareMatrix
import local.LocalSearch
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import shared.Instance
import shared.Solution
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.random.nextULong
import kotlin.system.measureTimeMillis


const val instanceId = 6

fun main() {
    val logger: Logger = LogManager.getLogger("main")
    Configurator.setRootLevel(Level.TRACE)

    val solution = Solution.fromFile(File("src/main/resources/results/best/i$instanceId.txt"))
    val instance = Instance.fromInstanceId(instanceId)

    Distances.initDistances(instance)

    val solutionBuilder = Ant(  // fake ant
        instance,
        FlatSquareMatrix(instance.nodes.size) { _, _ -> 0.0 },
        Config.Ant(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    ).SolutionBuilder()

    solutionBuilder.routes.removeLast()
    for (route in solution.routes) {
        solutionBuilder.createNewRoute()
        for (i in 1 until route.nodes.size) {
            solutionBuilder.currentRoute.addNextNode(instance.nodes[route.nodes[i].first])
        }
    }

    logger.info("Before:\n" + Solution.fromSolutionBuilder(solutionBuilder).formatOutput())

    val runtime = measureTimeMillis {
        LocalSearch(instance, solutionBuilder).search()
    }
    logger.info("RUNTIME LOCAL SEARCH: $runtime ms")

    logger.info("Final:\n" + Solution.fromSolutionBuilder(solutionBuilder).formatOutput())

    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
    Solution.fromSolutionBuilder(solutionBuilder)
        .exportToFile("src/main/resources/results/i$instanceId-LS-${formattedTimestamp}-${Random.nextULong()}.txt")
}
