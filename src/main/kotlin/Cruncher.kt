import java.io.File

fun main() {
    val lines = File("src/main/resources/graph/results.txt").readLines()

    val values: MutableMap<Int, MutableList<Pair<Int, Double>>> = mutableMapOf()

    (1..6).forEach { values[it] = arrayListOf() }

    lines.forEach {
        val split: List<String> = it.split(";")
        values.computeIfPresent(split[0].toInt()) { _, v ->
            v.also { it.add(Pair(split[1].toInt(), split[2].toDouble())) }
        }
    }

    values.entries.forEach {
        val vehicles = it.value.map { it.first }.average()
        val distances = it.value.map { it.second }.average()

        val best = it.value.sortedWith(compareBy({ it.first }, { it.second })).firstOrNull()

        println("instance: ${it.key}, vehicles: $vehicles, distances: $distances, best: $best")
    }

}