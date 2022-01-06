import helpers.ConfigChooser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Duration

fun main() {
    val instanceId = 1
    val runtime = Duration.ofSeconds(120)

    val base = ConfigChooser.getConfig(instanceId, runtime)

    File("src/main/resources/graph/config.json").appendText(
        Json.encodeToString(base) + System.lineSeparator()
    )

    while (true) {
        main2(base.deepCopy())
    }
}
