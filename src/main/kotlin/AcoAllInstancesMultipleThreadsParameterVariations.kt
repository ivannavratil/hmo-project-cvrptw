import helpers.ConfigChooser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import shared.Instance
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.ThreadPoolExecutor
import kotlin.math.pow

fun main() {
    val logger: Logger = LogManager.getLogger("main")

    val timeLimitMarker = "5m"

    val exportNames = (1..6).associateWith { "res-$timeLimitMarker-i$it" }
    val instances = (1..6).associateWith { Instance.fromInstanceId(it) }
    val baseConfigs = (1..6).associateWith { ConfigChooser.getConfig(it, timeLimitMarker) }

    val executor = Executors.newFixedThreadPool(6) as ThreadPoolExecutor

    while (true) {
        if (executor.queue.size > 50) {
            Thread.sleep(60 * 1000)
            continue
        }

        for (instanceId in 1..6) {
            val exportName = exportNames[instanceId]!!
            val instance = instances[instanceId]!!
            val baseConfig = baseConfigs[instanceId]!!

            executor.execute {
                val cAlpha = baseConfig.deepCopy()
                cAlpha.ant.alpha = ThreadLocalRandom.current().nextInt(0, 21).toDouble() / 10
                searchWithExports(instance, cAlpha, "$exportName-alpha", logger, cAlpha.ant.alpha)
            }

            executor.execute {
                val cBeta = baseConfig.deepCopy()
                cBeta.ant.beta = ThreadLocalRandom.current().nextInt(0, 21).toDouble() / 10
                searchWithExports(instance, cBeta, "$exportName-beta", logger, cBeta.ant.beta)
            }

            executor.execute {
                val cCount = baseConfig.deepCopy()
                cCount.ant.count = ThreadLocalRandom.current().nextInt(1, 11) * 10
                searchWithExports(instance, cCount, "$exportName-count", logger, cCount.ant.count)
            }

            executor.execute {
                val cq0 = baseConfig.deepCopy()
                cq0.ant.q0 = ThreadLocalRandom.current().nextInt(0, 11).toDouble() / 10
                searchWithExports(instance, cq0, "$exportName-q0", logger, cq0.ant.q0)
            }

            executor.execute {
                val cRho = baseConfig.deepCopy()
                cRho.ant.rho = ThreadLocalRandom.current().nextInt(0, 21).toDouble() * 4 / 100
                searchWithExports(instance, cRho, "$exportName-rho", logger, cRho.ant.rho)
            }

            executor.execute {
                val cTau = baseConfig.deepCopy()
                cTau.antColony.tauZero = 10.0.pow(ThreadLocalRandom.current().nextInt(-8, 0).toDouble())
                searchWithExports(instance, cTau, "$exportName-tau", logger, cTau.antColony.tauZero)
            }

            executor.execute {
                val cTheta = baseConfig.deepCopy()
                cTheta.ant.theta = ThreadLocalRandom.current().nextInt(0, 11).toDouble() / 10
                searchWithExports(instance, cTheta, "$exportName-theta", logger, cTheta.ant.theta)
            }
        }
    }
}
