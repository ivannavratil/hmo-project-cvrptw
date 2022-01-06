package helpers

import java.time.Duration

interface ITerminationCriteria {
    fun terminate(): Boolean  // TODO Take iterations or non-improving?
}

// TODO Add termination for N iterations without improvement and for total runtime, and their configs (with composite?)

class TotalTimeTermination(runtime: Duration) : ITerminationCriteria {
    private val endTimeMs = System.currentTimeMillis() + runtime.toMillis()

    override fun terminate(): Boolean {
        return System.currentTimeMillis() > endTimeMs
    }
}
