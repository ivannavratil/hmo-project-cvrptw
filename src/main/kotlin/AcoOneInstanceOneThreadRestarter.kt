import helpers.ConfigChooser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    val instanceId = 1
    val runtimeSeconds = 120.0

    val base = ConfigChooser.getConfig(instanceId, runtimeSeconds)

    File("src/main/resources/graph/config.json").appendText(
        Json.encodeToString(base) + System.lineSeparator()
    )

    while (true) {
        main2(base.deepCopy())
    }
}
