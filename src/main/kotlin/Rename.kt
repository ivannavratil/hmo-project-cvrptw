import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

fun main() {

    val dir = File("src/main/resources/results/best")

    val dirExport = File("src/main/resources/results/best-export")

    if (!dirExport.exists()) {
        Files.createDirectory(Path.of("src/main/resources/results/best-export"))
    }

    if (dir.isDirectory) { // make sure it's a directory
        for (f in dir.listFiles()) {
            try {

                if (f.name.contains(".DS_Store") || f.name.contains("ACO") || f.name.contains("meta")) {
                    continue
                }

                val split = f.name.split("-")

                val last = split.last().split(".")[1]

                val newName =
                    "src/main/resources/results/best-export/" + split[0] + "-" + split[1] + "-" + split[2] + "." + last

                Files.copy(f.toPath(), Path.of(newName), StandardCopyOption.REPLACE_EXISTING)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

}