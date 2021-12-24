data class Node(
    val id: Int,
    val xCoordinate: Int,
    val yCoordinate: Int,
    val demand: Int,
    val readyTime: Int,
    val dueTime: Int,
    val serviceTime: Int
) {
    companion object {
        fun fromLine(array: IntArray): Node {
            return Node(array[0], array[1], array[2], array[3], array[4], array[5], array[6])
        }
    }
}
