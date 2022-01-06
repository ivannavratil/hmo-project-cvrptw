package helpers

import java.time.Duration
import java.time.Instant

interface ITerminationCriteria {
    fun terminate(iteration: Int = -1): Boolean
}

// TODO Add termination for N iterations without improvement and for total runtime, and their configs (with composite?)

class TotalTimeTermination(runtime: Duration) : ITerminationCriteria {
    private val endTime = Instant.now() + runtime

    override fun terminate(iteration: Int): Boolean {
        return Instant.now() > endTime
    }
}

class TotalIterationsTermination(private val maxIterations: Int) : ITerminationCriteria {
    override fun terminate(iteration: Int): Boolean {
        if (iteration < 0) {
            throw IllegalArgumentException("$iteration")
        }
        return iteration > maxIterations
    }
}

class CompositeTermination(private vararg val criteria: ITerminationCriteria) : ITerminationCriteria {
    init {
        if (criteria.isEmpty()) {
            throw IllegalArgumentException("no criteria")
        }
    }

    override fun terminate(iteration: Int): Boolean {
        for (criterion in criteria) {
            if (criterion.terminate(iteration)) {
                return true
            }
        }
        return false
    }
}
