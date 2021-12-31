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

fun main() {

//    INSTANCE 1
    val base = Config(
        1, Int.MAX_VALUE,
        Config.Ant(80, 0.7, 1.2, 3.0, 0.55, 0.2, 0.08),
        Config.AntColony(tauZero = 1E-3)
    )

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
//    val base = Config(
//        6, Int.MAX_VALUE,
//        Config.Ant(6, 0.9, 1.25, 3.0, 0.6, 0.4, 0.2),
//        Config.AntColony(tauZero = 1E-6)
//    )

    File("src/main/resources/graph/config.json").appendText(
        Json.encodeToString(base) + System.lineSeparator()
    )

    while (true) {
        main2(base.copy())
    }
}

