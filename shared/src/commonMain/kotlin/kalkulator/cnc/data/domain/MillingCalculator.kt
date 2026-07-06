package kalkulator.cnc.data.domain

import kotlin.math.PI

data class CalculationResult(
    val f: Double = 0.0,
    val s: Double = 0.0,
    val fz: Double = 0.0,
    val center: Double = 0.0,
    val rm: Double = 0.0,
    val hv: Double = 0.0,
    val hb: Double = 0.0,
    val hrc: Double = 0.0,
    val isHardnessMode: Boolean = false,
    val isCenterMode: Boolean = false,
    val isSawMode: Boolean = false,
    val error: String? = null
)

object MillingCalculator {
    fun calculate(
        operationIndex: Int,
        vals: List<Double>,
        isMetric: Boolean
    ): CalculationResult {
        val calcSys = if (isMetric) 1000 else 12

        // Walidacja pustych zer (poza specyficznymi trybami)
        if (operationIndex != 8 && operationIndex != 9 && vals.any { it <= 0.0 }) {
            return CalculationResult(error = "Wypełnij poprawnie wszystkie wymagane pola.")
        }

        return try {
            when (operationIndex) {
                0 -> { // Frezowanie
                    val s = (vals[0] * calcSys) / (PI * vals[1])
                    val f = vals[2] * vals[3] * s
                    CalculationResult(f = f, s = s)
                }
                1, 7 -> { // Gwintowanie / Wytaczanie
                    val s = (vals[0] * calcSys) / (PI * vals[1])
                    val f = vals[2] * s
                    CalculationResult(f = f, s = s)
                }
                2 -> CalculationResult(
                    fz = (vals[0] * vals[1]) / (vals[3] * vals[2] * calcSys),
                    isSawMode = true
                )
                3 -> CalculationResult(
                    fz = (vals[0] * vals[3]) / (vals[1] * vals[2] * calcSys),
                    isSawMode = true
                )
                4 -> CalculationResult(
                    fz = (vals[0] * vals[2] * PI) / (vals[4] * vals[1] * vals[3] * calcSys),
                    isSawMode = true
                )
                5 -> CalculationResult(
                    fz = (vals[0] * vals[3] * PI) / (vals[1] * vals[2] * calcSys),
                    isSawMode = true
                )
                6 -> CalculationResult(f = vals[2] * ((vals[0] - vals[1]) / vals[0]))
                8 -> CalculationResult(center = (vals[0] + vals[1]) / 2.0, isCenterMode = true)
                9 -> handleHardness(vals)
                else -> CalculationResult(error = "Nieznana operacja")
            }
        } catch (e: Exception) {
            CalculationResult(error = "Błąd obliczeń: ${e.message}")
        }
    }

    private fun handleHardness(vals: List<Double>): CalculationResult {
        val filledCount = vals.count { it > 0.0 }
        if (filledCount == 0) return CalculationResult(error = "Wprowadź wartość twardości.")
        if (filledCount > 1) return CalculationResult(error = "Możesz wypełnić tylko jedno pole naraz.")

        val inputIndex = vals.indexOfFirst { it > 0.0 }
        val res = HardnessConverter.calculate(inputIndex, vals[inputIndex])

        return if (res != null) {
            CalculationResult(
                rm = res.rm, hv = res.hv, hb = res.hb, hrc = res.hrc,
                isHardnessMode = true
            )
        } else {
            CalculationResult(error = "Wartość poza zakresem konwersji.")
        }
    }
}

object HardnessConverter {
    data class Entry(val rm: Double, val hv: Double, val hb: Double, val hrc: Double)

    private val table = listOf(
        Entry(255.0, 80.0, 76.0, 0.0), Entry(320.0, 100.0, 95.0, 0.0),
        Entry(480.0, 150.0, 143.0, 0.0), Entry(640.0, 200.0, 190.0, 13.2),
        Entry(800.0, 250.0, 238.0, 22.2), Entry(970.0, 300.0, 285.0, 29.8),
        Entry(1130.0, 350.0, 333.0, 35.5), Entry(1290.0, 400.0, 380.0, 41.3),
        Entry(1450.0, 450.0, 428.0, 45.3), Entry(1620.0, 500.0, 475.0, 49.1),
        Entry(1930.0, 600.0, 570.0, 55.2), Entry(2200.0, 700.0, 665.0, 59.7)
    )

    fun calculate(typeIndex: Int, value: Double): Entry? {
        val sortedTable = table.sortedBy { getValByIndex(it, typeIndex) }
        if (value < getValByIndex(sortedTable.first(), typeIndex) ||
            value > getValByIndex(sortedTable.last(), typeIndex)
        ) return null

        for (i in 0 until sortedTable.size - 1) {
            val low = sortedTable[i]
            val high = sortedTable[i + 1]
            val vLow = getValByIndex(low, typeIndex)
            val vHigh = getValByIndex(high, typeIndex)

            if (value in vLow..vHigh) {
                val t = (value - vLow) / (vHigh - vLow)
                return Entry(
                    low.rm + t * (high.rm - low.rm), low.hv + t * (high.hv - low.hv),
                    low.hb + t * (high.hb - low.hb), low.hrc + t * (high.hrc - low.hrc)
                )
            }
        }
        return null
    }

    private fun getValByIndex(e: Entry, index: Int) = when (index) {
        0 -> e.rm; 1 -> e.hv; 2 -> e.hb; else -> e.hrc
    }
}