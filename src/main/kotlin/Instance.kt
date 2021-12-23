import java.io.File

data class Instance(
    val numberOfVehicles: Int,
    val capacity: Int,
    val nodes: List<Node>
) {
    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        fun fromFile(file: File): Instance {
            val buffer = file.readLines()
            val (numberOfVehicles, capacity) = buffer[2].parseInts()
            val nodes = IntRange(7, buffer.size - 1).map { Node.fromLine(buffer[it].parseInts()) }.toList()
            return Instance(numberOfVehicles, capacity, nodes)
        }

        fun fromInstanceId(id: Int): Instance {
            return fromFile(File("src/main/resources/i${id}"))
        }
    }
}
