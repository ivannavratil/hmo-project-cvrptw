package sa

interface TerminationCriteriaInterface {
    fun terminate(currentTemperature: Double): Boolean
}

class FinalTemperatureTermination(private val finalTemperature: Double) : TerminationCriteriaInterface {
    override fun terminate(currentTemperature: Double): Boolean {
        return currentTemperature < finalTemperature
    }
}

// TODO Add termination for N iterations without improvement and for total runtime, and their configs (with composite?)

class TotalTimeTermination(runtimeSeconds: Double) : TerminationCriteriaInterface {
    private val endTimeMs = System.currentTimeMillis() + (runtimeSeconds * 1000).toLong()

    override fun terminate(currentTemperature: Double): Boolean {
        return System.currentTimeMillis() > endTimeMs
    }
}
