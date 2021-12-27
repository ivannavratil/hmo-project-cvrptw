// TODO Remove all F64Array if calculations are not vectorized.

fun main() {
    val instanceId = 1
    val instance = Instance.fromInstanceId(instanceId)

    val aco = AntColony(1.0, instance)
    val (distances, inverseDistances) = AntColony.calculateDistances(instance)

    for (i in instance.nodes.indices) {
        for (j in instance.nodes.indices)
            print(String.format("%6.2f", inverseDistances[i, j]))
        println()
    }
}
