package helpers

fun String.parseDoubles(): DoubleArray {
    return this.trim().split("\\s++".toRegex()).map { it.toDouble() }.toDoubleArray()
}

fun String.parseInts(): IntArray {
    return this.trim().split("\\s++".toRegex()).map { it.toInt() }.toIntArray()
}

fun DoubleArray.argmax(): Int {
    var maxIndex = 0
    for (i in 1 until size)
        if (get(i) > get(maxIndex))
            maxIndex = i
    return maxIndex
}
