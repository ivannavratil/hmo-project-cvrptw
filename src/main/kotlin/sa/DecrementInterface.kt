package sa

interface DecrementInterface {
    fun decrement(temperature: Double): Double
}

class LinearDecrement(private val constant: Double) : DecrementInterface {
    override fun decrement(temperature: Double): Double {
        return temperature - constant
    }
}

class GeometricDecrement(private val alpha: Double) : DecrementInterface {
    override fun decrement(temperature: Double): Double {
        return alpha * temperature
    }
}

class VerySlowDecrease(private val beta: Double) : DecrementInterface {
    override fun decrement(temperature: Double): Double {
        return (temperature) / (1 + beta * temperature)
    }
}