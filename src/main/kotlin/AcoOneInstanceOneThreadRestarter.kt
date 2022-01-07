import helpers.ConfigChooser
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import shared.Instance

fun main() {
    val logger: Logger = LogManager.getLogger("main")
    Configurator.setRootLevel(Level.TRACE)

    val instanceId = 1
    val timeLimitMarker = "5m"
    val exportName = "res-$timeLimitMarker-i$instanceId"

    val instance = Instance.fromInstanceId(instanceId)
    val config = ConfigChooser.getConfig(instanceId, timeLimitMarker)

    while (true) {
        searchWithExports(instance, config.deepCopy(), exportName, logger)
    }
}
