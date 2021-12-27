data class Node(
    val id: Int,
    val xCoordinate: Double,
    val yCoordinate: Double,
    val demand: Int,
    val readyTime: Int,
    val dueTime: Int,
    val serviceTime: Int
) {
    companion object {
        fun fromLine(array: DoubleArray): Node {
            return Node(
                array[0].toInt(),
                array[1],
                array[2],
                array[3].toInt(),
                array[4].toInt(),
                array[5].toInt(),
                array[6].toInt()
            )
        }
    }
}
