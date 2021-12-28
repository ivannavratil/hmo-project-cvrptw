package sa

interface TerminationCriteriaInterface {
    fun terminate(currentTemperature: Double): Boolean
}

// TODO Add termination for N iterations without improvement and for total runtime

class FinalTemperatureTermination(private val finalTemperature: Double) : TerminationCriteriaInterface {
    override fun terminate(currentTemperature: Double): Boolean {
        return currentTemperature < finalTemperature
    }
}
