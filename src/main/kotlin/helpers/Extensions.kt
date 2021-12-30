package helpers

fun String.parseDoubles(): DoubleArray {
    return this.trim().split("\\s++".toRegex()).map { it.toDouble() }.toDoubleArray()
}

fun String.parseInts(): IntArray {
    return this.trim().split("\\s++".toRegex()).map { it.toInt() }.toIntArray()
}

fun DoubleArray.argmax(): Int {
    var maxIndex = 0
    var i = 1
    while (i < size) {
        if (get(i) > get(maxIndex)) maxIndex = i
        i++
    }
    return maxIndex
}
