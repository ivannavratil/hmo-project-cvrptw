package sa

import kotlin.math.abs
import kotlin.math.exp

interface SolutionAcceptanceInterface {
    fun acceptSolution(current: Int, possible: Int, temperature: Double): Boolean
}

class StandardAcceptance : SolutionAcceptanceInterface {
    override fun acceptSolution(current: Int, possible: Int, temperature: Double): Boolean {
        if (possible > current) return true

        val probabilityToAccept = exp((-abs(current - possible)) / temperature)

        return probabilityToAccept > Math.random()
    }
}

class Standard2Acceptance : SolutionAcceptanceInterface {
    override fun acceptSolution(current: Int, possible: Int, temperature: Double): Boolean {
        val probabilityToAccept = 1 / (1 + exp((current - possible) / temperature))
        return probabilityToAccept > Math.random()
    }
}