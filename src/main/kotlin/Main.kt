import java.io.File

fun main() {
    val instance = 4
    val buffer = File("src/main/resources/i$instance").readLines()

    val (numberOfVehicle, capacity) = buffer[2].parse()
    val depot: Depot = Depot.fromLine(buffer[7].parse())
    val customers = IntRange(8, buffer.size - 1).map { Customer.fromLine(buffer[it].parse()) }.toList()

}