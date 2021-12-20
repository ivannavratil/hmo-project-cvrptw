data class Customer(
    val id: Int,
    val xCoordinate: Int,
    val yCoordinate: Int,
    val demand: Int,
    val readyTime: Int,
    val dueDate: Int,
    val serviceTime: Int
) {
    companion object {
        fun fromLine(array: IntArray): Customer {
            return Customer(array[0], array[1], array[2], array[3], array[4], array[5], array[6])
        }
    }
}