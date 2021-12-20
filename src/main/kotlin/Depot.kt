data class Depot(
    val xCoordinate: Int,
    val yCoordinate: Int,
    val dueDate: Int,
) {
    companion object {
        fun fromLine(array: IntArray): Depot {
            return Depot(array[1], array[2], array[5])
        }
    }
}