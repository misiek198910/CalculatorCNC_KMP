package kalkulator.cnc.data.domain

data class ToleranceValue(
    val minDia: Double,
    val maxDia: Double,
    val upperUm: Double,
    val lowerUm: Double
)

data class IsoTolerance(
    val label: String,
    val isHole: Boolean,
    val values: List<ToleranceValue>
)

data class FitResult(
    val holeMin: Double,
    val holeMax: Double,
    val shaftMin: Double,
    val shaftMax: Double,
    val type: FitType,
    val maxClearance: Double,
    val minClearance: Double
)

enum class FitType {
    CLEARANCE,
    TRANSITION,
    INTERFERENCE
}