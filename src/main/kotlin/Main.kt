// TODO Remove all F64Array if calculations are not vectorized.

fun main() {
    val instanceId = 1
    val instance = Instance.fromInstanceId(instanceId)

    val aco = AntColony(1.0, instance)
    val (distances, inverseDistances) = AntColony.calculateDistances(instance)

    for (i in 0 until instance.nodes.size) {
        for (j in 0 until instance.nodes.size)
            print(String.format("%6.2f", inverseDistances[i, j]))
        println()
    }
}
