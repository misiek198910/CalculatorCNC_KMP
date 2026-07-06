package kalkulator.cnc.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "cnc_tools", primaryKeys = ["articleNumber", "materialGroup"])
data class CncToolEntity(
    val articleNumber: String,
    val toolCategory: String,
    val toolModelName: String,
    val materialGroup: String,
    val diameter: Double,
    val flutesCount: Int,
    val vcMin: Double,
    val vcMax: Double,
    val feedStep: Double,
    @ColumnInfo(name = "category_code") val categoryCode: String = "UNKNOWN"
)