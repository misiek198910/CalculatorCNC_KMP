package kalkulator.cnc.data.domain

import kotlin.math.PI
import kotlin.math.atan
import calculatorcnc.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class TurningResultMode { STANDARD, TIME, VOLUME, ROUGHNESS, TAPER, NONE }

data class TurningCalculationResult(
    val f: Double = 0.0,
    val s: Double = 0.0,
    val tc: Double = 0.0,
    val q: Double = 0.0,
    val ra: Double = 0.0,
    val angle: Double = 0.0,
    val mode: TurningResultMode = TurningResultMode.NONE,
    val errorRes: StringResource? = null,
    val errorArg: String? = null
)

object TurningCalculator {
    fun calculate(
        operationIndex: Int,
        vals: List<Double>,
        isMetric: Boolean
    ): TurningCalculationResult {
        val calcSys = if (isMetric) 1000 else 12

        if (vals.any { it <= 0.0 } && operationIndex != 6) {
            return TurningCalculationResult(errorRes = Res.string.error_fill_required)
        }

        return try {
            when (operationIndex) {
                0, 1, 2 -> {
                    // STANDARD / WYTACZANIE / GWINTOWANIE
                    // vals: [0] = DM, [1] = VC, [2] = Fn
                    val n = (vals[1] * calcSys) / (PI * vals[0])
                    val vf = vals[2] * n
                    TurningCalculationResult(s = n, f = vf, mode = TurningResultMode.STANDARD)
                }
                3 -> {
                    // CZAS
                    // vals: [0] = L, [1] = DM, [2] = VC, [3] = Fn
                    val n = (vals[2] * calcSys) / (PI * vals[1])
                    val tc = vals[0] / (vals[3] * n)
                    TurningCalculationResult(tc = tc, mode = TurningResultMode.TIME)
                }
                4 -> {
                    // OBJĘTOŚĆ
                    // vals: [0] = AP, [1] = Fn, [2] = VC
                    TurningCalculationResult(q = vals[0] * vals[1] * vals[2], mode = TurningResultMode.VOLUME)
                }
                5 -> {
                    // CHROPOWATOŚĆ (Ra)
                    // vals: [0] = Fn, [1] = R (Promień płytki)
                    val ra = ((vals[0] * vals[0]) / (32 * vals[1])) * 1000
                    TurningCalculationResult(ra = ra, mode = TurningResultMode.ROUGHNESS)
                }
                6 -> {
                    // STOŻEK
                    // vals: [0] = D, [1] = d, [2] = L
                    val angle = atan((vals[0] - vals[1]) / (2 * vals[2])) * (180.0 / PI)
                    TurningCalculationResult(angle = angle, mode = TurningResultMode.TAPER)
                }
                else -> TurningCalculationResult(errorRes = Res.string.error_unknown_operation)
            }
        } catch (e: Exception) {
            TurningCalculationResult(
                errorRes = Res.string.error_calc_exception,
                errorArg = e.message ?: "Unknown"
            )
        }
    }
}