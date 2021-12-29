package sa

import helpers.seededRandom
import kotlin.math.exp

interface SolutionAcceptanceInterface {
    fun acceptSolution(current: Double, neighbor: Double, temperature: Double): Boolean
}

class StandardAcceptance : SolutionAcceptanceInterface {
    override fun acceptSolution(current: Double, neighbor: Double, temperature: Double): Boolean {
        if (neighbor < current)
            return true
        val probabilityToAccept = exp((current - neighbor) / temperature)
        return seededRandom.nextDouble() < probabilityToAccept
    }
}

class Standard2Acceptance : SolutionAcceptanceInterface {
    override fun acceptSolution(current: Double, neighbor: Double, temperature: Double): Boolean {
        val probabilityToAccept = 1.0 / (1 + exp((neighbor - current) / temperature))
        return seededRandom.nextDouble() < probabilityToAccept
    }
}
