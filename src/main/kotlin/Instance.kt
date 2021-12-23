import java.io.File

data class Instance(
    val numberOfVehicles: Int,
    val capacity: Int,
    val depot: Depot,
    val customers: List<Customer>
) {
    companion object {
        @Suppress("MemberVisibilityCanBePrivate")
        fun fromFile(file: File): Instance {
            val buffer = file.readLines()

            val (numberOfVehicles, capacity) = buffer[2].parseInts()
            val depot = Depot.fromLine(buffer[7].parseInts())
            val customers = IntRange(8, buffer.size - 1).map { Customer.fromLine(buffer[it].parseInts()) }.toList()

            return Instance(numberOfVehicles, capacity, depot, customers)
        }

        fun fromInstanceId(id: Int): Instance {
            return fromFile(File("src/main/resources/i${id}"))
        }
    }
}
