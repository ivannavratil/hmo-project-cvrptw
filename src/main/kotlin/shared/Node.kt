package shared

data class Node(
    val id: Int,
    val xCoordinate: Double,
    val yCoordinate: Double,
    val demand: Int,
    val readyTime: Int,
    val dueTime: Int,
    val serviceTime: Int
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Node
        return id == other.id
    }

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
