fun String.parse(): IntArray {
    return this.trim().split("\\s++".toRegex()).map { it.toInt() }.toIntArray()
}