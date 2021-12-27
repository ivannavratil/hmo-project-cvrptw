// TODO Remove all F64Array if calculations are not vectorized.
// TODO Array / ArrayList / List ?
// TODO Sparse structures?

fun main() {
    val instanceId = 6
    val instance = Instance.fromInstanceId(instanceId)

    val aco = AntColony(instance, startingTemperature = 100.0)

    aco.run(100)
    Solution.fromSolutionBuilder(aco.incumbentSolution!!).exportToFile("src/main/resources/results/i$instanceId.txt")
}
