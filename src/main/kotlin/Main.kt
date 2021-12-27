import Distances.inverseDistances

// TODO Remove all F64Array if calculations are not vectorized.
// TODO Array / ArrayList / List ?
// TODO Sparse structures?

fun main() {
    val instanceId = 1
    val instance = Instance.fromInstanceId(instanceId)

    val aco = AntColony(instance, 1.0, 100.0)

    for (i in instance.nodes.indices) {
        for (j in instance.nodes.indices)
            print(String.format("%6.2f", inverseDistances[i, j]))
        println()
    }
}
