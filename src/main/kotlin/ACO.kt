import aco.AntColony
import helpers.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import shared.Instance
import shared.Solution
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.system.measureTimeMillis

// TODO Remove all F64Array if calculations are not vectorized.
// TODO Array / ArrayList / List ?
// TODO Sparse structures?
fun copy(c: Config): Config = c.copy(ant = c.ant.copy(), antColony = c.antColony.copy())

fun main() {

    val base = Config(
        6, Int.MAX_VALUE,
        Config.Ant(69, 0.7, 1.1, 3.0, 0.5, 0.25, 0.08),
        Config.AntColony(tauZero = 0.0001)
    )

    File("src/main/resources/graph/i${base.instanceId}").appendText(
        Json.encodeToString(base) + System.lineSeparator()
    )

    //TODO: i1 - tau= 0.001

    thread {
        val cAlpha = copy(base)
        for (alpha in mutableListOf(0.6, 0.65, 0.7, 0.75, 0.8)) {
            cAlpha.ant.alpha = alpha
            main2(cAlpha, "alpha", alpha)
        }
    }

    thread {
        val cBeta = copy(base)
        for (beta in mutableListOf(0.5, 1.0, 1.25, 1.5, 1.75, 2.1, 2.5)) {
            cBeta.ant.beta = beta
            main2(cBeta, "beta", beta)
        }
    }

    thread {
        val cCount = copy(base)
        for (count in (10 until 100 step 10)) {
            cCount.ant.count = count
            main2(cCount, "count", count.toDouble())
        }
    }

    thread {
        val cqo = copy(base)
        for (q0 in mutableListOf(0.05, 0.10, 0.15, 0.20, 0.25, 0.35)) {
            cqo.ant.q0 = q0
            main2(cqo, "q0", q0)
        }
    }

    thread {
        val cRho = copy(base)
        for (rho in mutableListOf(0.001, 0.01, 0.03, 0.05, 0.1, 0.2)) {
            cRho.ant.rho = rho
            main2(cRho, "rho", rho)
        }
    }

    thread {
        val cTau = copy(base)
        for (tau in mutableListOf(1E-6, 1E-5, 1E-4, 5E-3, 1E-3, 1E-2, 1E-1)) {
            cTau.antColony.tauZero = tau
            main2(cTau, "tau", tau)
        }
    }

    thread {
        val cTheta = copy(base)
        for (theta in mutableListOf(0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8)) {
            cTheta.ant.theta = theta
            main2(cTheta, "theta", theta)
        }
    }

}

fun main2(config: Config, param: String, paramValue: Double) {
    val logger: Logger = LogManager.getLogger("main")
    Configurator.setRootLevel(Level.TRACE)

    println("helpers.Config setup: $config")

    val instance = Instance.fromInstanceId(config.instanceId)

    val aco = AntColony(instance, config.antColony)

    val runtime = measureTimeMillis {
        aco.run(config)
    }
    logger.info("RUNTIME: $runtime ms")

    val formattedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))

    Solution.fromSolutionBuilder(aco.incumbentSolution!!)
        .exportToFile("src/main/resources/results/i${config.instanceId}-${formattedTimestamp}-${Random.nextLong()}.txt")

//    File("src/main/resources/results/i${config.instanceId}-$formattedTimestamp.json").writeText(Json.encodeToString(config))

    val path = "src/main/resources/graph/i${config.instanceId}-$param.txt"

    File(path).appendText(
        "$paramValue;${aco.incumbentSolution!!.vehiclesUsed};${aco.incumbentSolution!!.totalDistance}" + System.lineSeparator()
    )

    //deserialization
    //Json.decodeFromString<Config>(File("src/main/resources/results/i${config.instanceId}-$timeStamp.json").readLines().first())
}
