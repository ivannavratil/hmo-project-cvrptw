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

// TODO Array / ArrayList / List ?

// TODO For instance 6:
// TODO alpha [0.7, 1.1] makes almost no difference
// TODO large beta significantly reduces distances at the cost of more cars
// TODO large theta results in longer distances, roughly same #cars

fun main() {
    val instanceId = 1
    val runtime = Duration.ofSeconds(120)

    val base = ConfigChooser.getConfig(instanceId, runtime)

    File("src/main/resources/graph/i$instanceId").appendText(
        Json.encodeToString(base) + System.lineSeparator()
    )

    for (i in 1..2) {

        thread(name = "alpha") {
            for (alpha in listOf(0.7, 0.8, 0.9, 1.0, 1.1)) {
                val cAlpha = base.deepCopy()
                cAlpha.ant.alpha = alpha
                main2(instanceId, cAlpha, "alpha", alpha)
            }
        }

        thread(name = "beta") {
            for (beta in listOf(1.0, 1.25, 1.5, 1.75, 2.0)) {
                val cBeta = base.deepCopy()
                cBeta.ant.beta = beta
                main2(instanceId, cBeta, "beta", beta)
            }
        }

        thread(name = "count") {
            for (count in (2..25 step 5)) {
                val cCount = base.deepCopy()
                cCount.ant.count = count
                main2(instanceId, cCount, "count", count.toDouble())
            }
        }

        thread(name = "q0") {
            for (q0 in listOf(0.25, 0.35, 0.4, 0.45, 0.5)) {
                val cq0 = base.deepCopy()
                cq0.ant.q0 = q0
                main2(instanceId, cq0, "q0", q0)
            }
        }

        thread(name = "rho") {
            for (rho in listOf(0.05, 0.1, 0.2, 0.3, 0.4)) {
                val cRho = base.deepCopy()
                cRho.ant.rho = rho
                main2(instanceId, cRho, "rho", rho)
            }
        }

        thread(name = "tau") {
            for (tau in listOf(1E-7, 5E-7, 1E-6, 5E-6, 1E-5)) {
                val cTau = base.deepCopy()
                cTau.antColony.tauZero = tau
                main2(instanceId, cTau, "tau", tau)
            }
        }

        thread(name = "theta") {
            for (theta in listOf(0.4, 0.5, 0.6, 0.7, 0.75)) {
                val cTheta = base.deepCopy()
                cTheta.ant.theta = theta
                main2(instanceId, cTheta, "theta", theta)
            }
        }
    }
}

fun main2(instanceId: Int, config: Config, param: String, paramValue: Double) {
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
        .exportToFile("src/main/resources/results/i$instanceId-${formattedTimestamp}-${Random.nextULong()}.txt")

//    File("src/main/resources/results/i${config.instanceId}-$formattedTimestamp.json").writeText(Json.encodeToString(config))

    val path = "src/main/resources/graph/i$instanceId-$param.txt"

    File(path).appendText(
        "$paramValue;${incumbentSolution.vehiclesUsed};${incumbentSolution.totalDistance}" + System.lineSeparator()
    )
}
