fun String.parseInts(): IntArray {
    return this.trim().split("\\s++".toRegex()).map { it.toInt() }.toIntArray()
}
