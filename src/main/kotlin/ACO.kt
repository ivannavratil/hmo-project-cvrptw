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

// TODO Array / ArrayList / List ?
// TODO Sparse structures?

// TODO For instance 6:
// TODO alpha [0.7, 1.1] makes almost no difference
// TODO large beta significantly reduces distances at the cost of more cars
// TODO large theta results in longer distances, roughly same #cars

fun copy(c: Config): Config = c.copy(ant = c.ant.copy(), antColony = c.antColony.copy())

fun main() {

//    INSTANCE 1
//    val base = Config(
//        1, Int.MAX_VALUE,
//        Config.Ant(80, 0.7, 1.2, 3.0, 0.55, 0.2, 0.08),
//        Config.AntColony(tauZero = 1E-3)
//    )

//    INSTANCE 2
//    val base = Config(
//        2, Int.MAX_VALUE,
//        Config.Ant(80, 1.0, 1.25, 3.0, 0.45, 0.4, 0.2),
//        Config.AntColony(tauZero = 2E-4)
//    )

//    INSTANCE 3
//    val base = Config(
//        3, Int.MAX_VALUE,
//        Config.Ant(20, 1.0, 1.4, 3.0, 0.5, 0.4, 0.2),
//        Config.AntColony(tauZero = 7E-5)
//    )

//    INSTANCE 4
//    val base = Config(
//        4, Int.MAX_VALUE,
//        Config.Ant(20, 1.1, 1.25, 3.0, 0.7, 0.4, 0.15),
//        Config.AntColony(tauZero = 6E-5)
//    )

//    INSTANCE 5
//    val base = Config(
//        5, Int.MAX_VALUE,
//        Config.Ant(14, 1.25, 1.2, 3.0, 0.72, 0.4, 0.15),
//        Config.AntColony(tauZero = 2E-5)
//    )

//    INSTANCE 6
    val base = Config(
        6, Int.MAX_VALUE,
        Config.Ant(6, 0.9, 1.25, 3.0, 0.6, 0.4, 0.2),
        Config.AntColony(tauZero = 1E-6)
    )

    File("src/main/resources/graph/i${base.instanceId}").appendText(
        Json.encodeToString(base) + System.lineSeparator()
    )

    for (i in 1..2) {

        thread(name = "alpha") {
            val cAlpha = copy(base)
            for (alpha in mutableListOf(0.7, 0.8, 0.9, 1.0, 1.1)) {
                cAlpha.ant.alpha = alpha
                main2(cAlpha, "alpha", alpha)
            }
        }

        thread(name = "beta") {
            val cBeta = copy(base)
            for (beta in mutableListOf(1.0, 1.25, 1.5, 1.75, 2.0)) {
                cBeta.ant.beta = beta
                main2(cBeta, "beta", beta)
            }
        }

        thread(name = "count") {
            val cCount = copy(base)
            for (count in (2..25 step 5)) {
                cCount.ant.count = count
                main2(cCount, "count", count.toDouble())
            }
        }

        thread(name = "q0") {
            val cq0 = copy(base)
            for (q0 in mutableListOf(0.25, 0.35, 0.4, 0.45, 0.5)) {
                cq0.ant.q0 = q0
                main2(cq0, "q0", q0)
            }
        }

        thread(name = "rho") {
            val cRho = copy(base)
            for (rho in mutableListOf(0.05, 0.1, 0.2, 0.3, 0.4)) {
                cRho.ant.rho = rho
                main2(cRho, "rho", rho)
            }
        }

        thread(name = "tau") {
            val cTau = copy(base)
            for (tau in mutableListOf(1E-7, 5E-7, 1E-6, 5E-6, 1E-5)) {
                cTau.antColony.tauZero = tau
                main2(cTau, "tau", tau)
            }
        }

        thread(name = "theta") {
            val cTheta = copy(base)
            for (theta in mutableListOf(0.4, 0.5, 0.6, 0.7, 0.75)) {
                cTheta.ant.theta = theta
                main2(cTheta, "theta", theta)
            }
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
