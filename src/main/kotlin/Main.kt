import aco.AntColony
import com.sksamuel.hoplite.ConfigLoader
import helpers.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import kotlin.random.nextUInt


private val jsonFormatter = Json { prettyPrint = true }

fun main(args: Array<String>) {
    val logger: Logger = LogManager.getLogger("main")
    Configurator.setRootLevel(Level.TRACE)

    if (args.size != 1 && args.size != 2) {
        logger.fatal("expected arguments: instance path, [config path]")
        return
    }

    val config = if (args.size == 2) {
        ConfigLoader().loadConfigOrThrow(File(args[1]))
    } else {
        Config(
            Config.Ant(50, 1.0, 1.25, 0.55, 0.35, 0.15),
            Config.AntColony(
                1000, Duration.ofSeconds(50), 1E-4, true,
                Config.LocalSearch(500, Duration.ofSeconds(6))
            ),
            Config.LocalSearch(2000, Duration.ofSeconds(10))
        )
    }
    logger.info("Config setup: $config")

    val instance = Instance.fromFile(File(args[0]))

    searchWithExports(instance, config, "result", logger)
}

fun searchWithExports(
    instance: Instance,
    config: Config,
    exportName: String,
    logger: Logger,
    variedParameter: Any? = null
): SolutionBuilder? {
    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
    val randomStamp = Random.nextUInt()
    val uniqueStamp = "$formattedTimestamp-$randomStamp"
    val solutionExportPathPart = "src/main/resources/results/$exportName-$uniqueStamp"

    val aco = AntColony(instance, config)

    val startTimeAco = Instant.now()
    val incumbentSolutionAco = aco.run()
    val runtimeAco = Duration.between(startTimeAco, Instant.now())

    if (incumbentSolutionAco == null) {
        logger.warn("ACO failed to find a solution")
        return null
    }

    Solution.fromSolutionBuilder(incumbentSolutionAco)
        .exportToFile("$solutionExportPathPart-ACO.txt")

    val localSearch = LocalSearch(instance, incumbentSolutionAco)

    val startTimeLs = Instant.now()
    val incumbentSolutionLs = localSearch.fullSearch(config.finalLocalSearch)
    val runtimeLs = Duration.between(startTimeLs, Instant.now())

    Solution.fromSolutionBuilder(incumbentSolutionLs)
        .exportToFile("$solutionExportPathPart-LS.txt")

    val resultMetadata = ResultMetadata(aco, localSearch, runtimeAco, runtimeLs, config)
    File("$solutionExportPathPart-meta.json").writeText(
        jsonFormatter.encodeToString(resultMetadata),
        Charsets.UTF_8
    )

    File("src/main/resources/graph/$exportName-ACO.txt").appendText(
        "${resultMetadata.totalRuntimeAco.toSeconds()};" +
                "${incumbentSolutionAco.vehiclesUsed};" +
                "${incumbentSolutionAco.totalDistance}" +
                (if (variedParameter != null) ";$variedParameter" else "") +
                System.lineSeparator()
    )
    File("src/main/resources/graph/$exportName-LS.txt").appendText(
        "${resultMetadata.totalRuntimeCombined.toSeconds()};" +
                "${incumbentSolutionLs.vehiclesUsed};" +
                "${incumbentSolutionLs.totalDistance}" +
                (if (variedParameter != null) ";$variedParameter" else "") +
                System.lineSeparator()
    )

    return incumbentSolutionLs
}
