package helpers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Duration

object DurationAsLongSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Duration", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Duration) = encoder.encodeLong(value.toMillis())
    override fun deserialize(decoder: Decoder): Duration = Duration.ofMillis(decoder.decodeLong())
}

// Total maximum runtime is equal to antColony.runtime + finalLocalSearch.runtime

@Serializable
data class Config(
    val ant: Ant,
    val antColony: AntColony,
    val finalLocalSearch: LocalSearch
) {
    fun deepCopy(): Config = this.copy(
        ant = this.ant.copy(),
        antColony = this.antColony.copy(),
        finalLocalSearch = this.finalLocalSearch.copy()
    )

    @Serializable
    data class Ant(
        var count: Int,
        var alpha: Double,
        var beta: Double,
        var theta: Double,
        var q0: Double,
        var rho: Double
    )

    @Serializable
    data class AntColony(
        val iterations: Int,
        @Serializable(with = DurationAsLongSerializer::class)
        val runtime: Duration,
        var tauZero: Double,
        val estimateTauZero: Boolean,
        val estimateLocalSearch: LocalSearch?  // counted within AntColony runtime
    ) {
        override fun toString(): String {
            return "AntColony(iterations=$iterations, runtime=${runtime.toSeconds()}s, tauZero=$tauZero, " +
                    "estimateTauZero=$estimateTauZero, estimateLocalSearch=$estimateLocalSearch)"
        }
    }

    @Serializable
    data class LocalSearch(
        val iterations: Int,
        @Serializable(with = DurationAsLongSerializer::class)
        val runtime: Duration
    ) {
        override fun toString(): String {
            return "LocalSearch(iterations=$iterations, runtime=${runtime.toSeconds()}s)"
        }
    }
}
