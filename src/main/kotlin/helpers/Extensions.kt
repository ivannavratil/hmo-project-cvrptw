package helpers

fun String.parseDoubles(): DoubleArray {
    return this.trim().split("\\s++".toRegex()).map { it.toDouble() }.toDoubleArray()
}

fun String.parseInts(): IntArray {
    return this.trim().split("\\s++".toRegex()).map { it.toInt() }.toIntArray()
}

fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
    return withIndex().maxByOrNull { it.value }?.index
}
