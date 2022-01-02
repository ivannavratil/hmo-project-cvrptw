import helpers.ConfigChooser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main() {

    val instanceId = 1

    val base = ConfigChooser.getConfig(instanceId)

    File("src/main/resources/graph/config.json").appendText(
        Json.encodeToString(base) + System.lineSeparator()
    )

    while (true) {
        main2(base.copy())
    }
}

