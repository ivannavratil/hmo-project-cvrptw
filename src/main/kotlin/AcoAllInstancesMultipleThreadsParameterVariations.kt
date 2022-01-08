import helpers.ConfigChooser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import shared.Instance
import java.util.Random
import kotlin.concurrent.thread
import kotlin.math.pow

fun main() {
    val logger: Logger = LogManager.getLogger("main")

    while (true) {

        for (instanceId in 1..6) {

            val timeLimitMarker = "5m"
            val exportName = "res-$timeLimitMarker-i$instanceId"

            val instance = Instance.fromInstanceId(instanceId)
            val baseConfig = ConfigChooser.getConfig(instanceId, timeLimitMarker)

            val alpha = thread(name = "alpha") {
                val cAlpha = baseConfig.deepCopy()
                cAlpha.ant.alpha = Random().nextInt(0, 20).toDouble() / 10
                searchWithExports(instance, cAlpha, "$exportName-alpha", logger, cAlpha.ant.alpha)
            }

            val beta = thread(name = "beta") {
                val cBeta = baseConfig.deepCopy()
                cBeta.ant.beta = Random().nextInt(0, 20).toDouble() / 10
                searchWithExports(instance, cBeta, "$exportName-beta", logger, cBeta.ant.beta)
            }

            val count = thread(name = "count") {
                val cCount = baseConfig.deepCopy()
                cCount.ant.count = Random().nextInt(1, 20) * 5
                searchWithExports(instance, cCount, "$exportName-count", logger, cCount.ant.count)
            }

            val q0 = thread(name = "q0") {
                val cq0 = baseConfig.deepCopy()
                cq0.ant.q0 = Random().nextInt(0, 20).toDouble() / 10
                searchWithExports(instance, cq0, "$exportName-q0", logger, cq0.ant.q0)
            }

            val rho = thread(name = "rho") {
                val cRho = baseConfig.deepCopy()
                cRho.ant.rho = Random().nextInt(0, 10).toDouble() / 100
                searchWithExports(instance, cRho, "$exportName-rho", logger, cRho.ant.rho)
            }

            val tau = thread(name = "tau") {
                val cTau = baseConfig.deepCopy()
                cTau.antColony.tauZero = 1.0.pow(Random().nextInt(-8, -1).toDouble())
                searchWithExports(instance, cTau, "$exportName-tau", logger, cTau.antColony.tauZero)
            }

            val theta = thread(name = "theta") {
                val cTheta = baseConfig.deepCopy()
                cTheta.ant.theta = Random().nextInt(0, 10).toDouble() / 10
                searchWithExports(instance, cTheta, "$exportName-theta", logger, cTheta.ant.theta)
            }

            alpha.join()
            beta.join()
            count.join()
            q0.join()
            rho.join()
            tau.join()
            theta.join()

            logger.info("All threads restarting!")
        }
    }
}

