import com.sksamuel.hoplite.ConfigLoader
import data.AntColony
import data.Instance
import data.Solution
import helpers.Config

// TODO Remove all F64Array if calculations are not vectorized.
// TODO Array / ArrayList / List ?
// TODO Sparse structures?

fun main() {
    val config = try {
        ConfigLoader().loadConfigOrThrow("/config.yaml")
    } catch (ex: Exception) {
        println("Using values set programmatically!")
        Config(1, 1000, Config.Ant(69, 1.0, 2.0, 3.0, 0.75, 0.75, 0.1), Config.AntColony(0.001, 100.0))
    }
    println("helpers.Config setup: $config")

    val instance = Instance.fromInstanceId(config.instanceId)

    val aco = AntColony(instance, config.antColony)

    aco.run(config)
    Solution.fromSolutionBuilder(aco.incumbentSolution!!)
        .exportToFile("src/main/resources/results/i${config.instanceId}.txt")
}
