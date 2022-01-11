import java.io.File
import java.nio.file.Files
import java.nio.file.Path

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
                    "src/main/resources/results/best-export/" + split[0] + "-" + split[1] + "-" + split[2] + "-" + last

                if (f.renameTo(File(newName))) {
                    System.out.println("Rename succesful")
                } else {
                    System.out.println("Rename failed")
                }
            } catch (e: Exception) {
                // TODO: handle exception
                e.printStackTrace()
            }

        }
    }

}