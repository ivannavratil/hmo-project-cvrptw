package shared

import helpers.FlatSquareMatrix
import helpers.FlatSquareMatrixInt
import helpers.parseDoubles
import helpers.parseInts
import java.io.File
import kotlin.math.ceil
import kotlin.math.hypot

data class Instance(
    val numberOfVehicles: Int,
    val capacity: Int,
    val nodes: List<Node>
) {
    val depot = nodes[0]

    val distances = FlatSquareMatrix(nodes.size) { row, col ->
        val n1 = nodes[row]
        val n2 = nodes[col]
        hypot(n2.xCoordinate - n1.xCoordinate, n2.yCoordinate - n1.yCoordinate)
    }

    val inverseDistances = distances.copy().transformInPlace { 1.0 / it }  // infinity on diagonal
    val travelTime = FlatSquareMatrixInt(nodes.size) { row, col -> ceil(distances[row, col]).toInt() }


    companion object {
        fun fromFile(file: File): Instance {
            val buffer = file.readLines()
            val (numberOfVehicles, capacity) = buffer[2].parseInts()
            val nodes = IntRange(7, buffer.size - 1).map { Node.fromLine(buffer[it].parseDoubles()) }
            return Instance(numberOfVehicles, capacity, nodes)
        }

        fun fromInstanceId(id: Int) = fromFile(File("src/main/resources/i${id}"))
    }
}
