import Distances.inverseDistances

// TODO Remove all F64Array if calculations are not vectorized.
// TODO Array / ArrayList / List ?
// TODO Sparse structures?

fun main() {
    val instanceId = 1
    val instance = Instance.fromInstanceId(instanceId)

    for (i in instance.nodes.indices) {
        for (j in instance.nodes.indices)
            print(String.format("%6.2f", inverseDistances[i, j]))
        println()
    }

    val aco = AntColony(instance, startingTemperature = 100.0)
    aco.run(800)
    Solution.fromSolutionBuilder(aco.incumbentSolution!!).exportToFile("src/main/resources/results/test.txt")
}
