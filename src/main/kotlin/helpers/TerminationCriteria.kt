package helpers

interface ITerminationCriteria {
    fun terminate(): Boolean  // TODO Take iterations or non-improving?
}

// TODO Add termination for N iterations without improvement and for total runtime, and their configs (with composite?)

class TotalTimeTermination(runtimeSeconds: Double) : ITerminationCriteria {
    private val endTimeMs = System.currentTimeMillis() + (runtimeSeconds * 1000).toLong()

    override fun terminate(): Boolean {
        return System.currentTimeMillis() > endTimeMs
    }
}
