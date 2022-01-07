import aco.AntColony
import helpers.Config
import helpers.DurationAsStringSerializer
import kotlinx.serialization.Serializable
import local.LocalSearch
import java.time.Duration

@Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")
@Serializable
class ResultMetadata(
    val incumbentEvaluationsAco: Int,
    @Serializable(with = DurationAsStringSerializer::class)
    val incumbentTimeAco: Duration,
    val evaluationsAco: Int,
    val evaluationsAcoTauZero: Int,
    val incumbentEvaluationsLocal: Int,
    @Serializable(with = DurationAsStringSerializer::class)
    val incumbentTimeLocal: Duration,
    val evaluationsLocal: Int,
    @Serializable(with = DurationAsStringSerializer::class)
    val totalRuntimeAco: Duration,
    @Serializable(with = DurationAsStringSerializer::class)
    val totalRuntimeLocal: Duration,
    val config: Config
) {
    val totalIncumbentEvaluations = evaluationsAcoTauZero + incumbentEvaluationsAco + incumbentEvaluationsLocal

    @Serializable(with = DurationAsStringSerializer::class)
    val totalIncumbentTime: Duration = incumbentTimeAco + incumbentTimeLocal

    @Serializable(with = DurationAsStringSerializer::class)
    val totalRuntimeCombined: Duration = totalRuntimeAco + totalRuntimeLocal

    constructor(
        aco: AntColony,
        localSearch: LocalSearch,
        totalRuntimeAco: Duration,
        totalRuntimeLocal: Duration,
        config: Config
    ) : this(
        aco.incumbentEvaluations,
        aco.incumbentTime,
        aco.evaluations,
        aco.evaluationsTauZero,
        localSearch.incumbentEvaluations,
        localSearch.incumbentTime,
        localSearch.evaluations,
        totalRuntimeAco,
        totalRuntimeLocal,
        config
    )
}
