import helpers.ConfigChooser
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import shared.Instance
import shared.SolutionBuilder

fun main() {
    val logger: Logger = LogManager.getLogger("main")
    Configurator.setRootLevel(Level.TRACE)

    val instanceId = 1
    val timeLimitMarker = "5m"
    val exportName = "res-$timeLimitMarker-i$instanceId"

    val instance = Instance.fromInstanceId(instanceId)
    val config = ConfigChooser.getConfig(instanceId, timeLimitMarker)

    var incumbent: SolutionBuilder? = null

    while (true) {
        val foundSolution = searchWithExports(instance, config.deepCopy(), exportName, logger)
        if (incumbent == null || foundSolution != null && foundSolution < incumbent) {
            incumbent = foundSolution
            logger.info("GLOBAL BEST CHANGED: - vehicles: ${incumbent!!.vehiclesUsed}, distance: ${incumbent.totalDistance}")
        }
    }
}
