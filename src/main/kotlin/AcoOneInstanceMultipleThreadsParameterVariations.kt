import helpers.ConfigChooser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import shared.Instance
import kotlin.concurrent.thread


fun main() {
    val logger: Logger = LogManager.getLogger("main")
    //Configurator.setRootLevel(Level.TRACE)

    val instanceId = 4
    val timeLimitMarker = "un"
    val exportName = "res-$timeLimitMarker-i$instanceId"

    val instance = Instance.fromInstanceId(instanceId)
    val baseConfig = ConfigChooser.getConfig(instanceId, timeLimitMarker)


    while (true) {

        val alpha = thread(name = "alpha") {
            for (alpha in listOf(0.8, 0.9, 1.0, 1.1, 1.2)) {
                val cAlpha = baseConfig.deepCopy()
                cAlpha.ant.alpha = alpha
                searchWithExports(instance, cAlpha, "$exportName-alpha", logger, alpha)
            }
        }

        val beta = thread(name = "beta") {
            for (beta in listOf(0.5, 0.75, 1.0, 1.25, 1.5)) {
                val cBeta = baseConfig.deepCopy()
                cBeta.ant.beta = beta
                searchWithExports(instance, cBeta, "$exportName-beta", logger, beta)
            }
        }

        val count = thread(name = "count") {
            for (count in (2..25 step 5)) {
                val cCount = baseConfig.deepCopy()
                cCount.ant.count = count
                searchWithExports(instance, cCount, "$exportName-count", logger, count)
            }
        }

        val q0 = thread(name = "q0") {
            for (q0 in listOf(0.35, 0.4, 0.45, 0.5, 0.6)) {
                val cq0 = baseConfig.deepCopy()
                cq0.ant.q0 = q0
                searchWithExports(instance, cq0, "$exportName-q0", logger, q0)
            }
        }

        val rho = thread(name = "rho") {
            for (rho in listOf(0.01, 0.05, 0.1, 0.2, 0.3)) {
                val cRho = baseConfig.deepCopy()
                cRho.ant.rho = rho
                searchWithExports(instance, cRho, "$exportName-rho", logger, rho)
            }
        }

        val tau = thread(name = "tau") {
            for (tau in listOf(1E-7, 5E-7, 1E-6, 5E-6, 1E-5)) {
                val cTau = baseConfig.deepCopy()
                cTau.antColony.tauZero = tau
                searchWithExports(instance, cTau, "$exportName-tau", logger, tau)
            }
        }

        val theta = thread(name = "theta") {
            for (theta in listOf(0.4, 0.5, 0.6, 0.7, 0.75)) {
                val cTheta = baseConfig.deepCopy()
                cTheta.ant.theta = theta
                searchWithExports(instance, cTheta, "$exportName-theta", logger, theta)
            }
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
