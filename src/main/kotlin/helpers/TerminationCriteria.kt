package helpers

import java.time.Duration
import java.time.Instant

interface ITerminationCriteria {
    fun terminate(iteration: Int = -1): Boolean
}

class TotalTimeTermination(private val endTime: Instant) : ITerminationCriteria {

    constructor(runtime: Duration) : this(Instant.now() + runtime)

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
