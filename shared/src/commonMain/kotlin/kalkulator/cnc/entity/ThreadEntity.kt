package kalkulator.cnc.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "threads")
@Serializable
data class ThreadEntity(
    @PrimaryKey
    val id: Int,

    @SerialName("category_id")
    @ColumnInfo(name = "category_id")
    val categoryId: Int,

    @SerialName("table_id")
    @ColumnInfo(name = "table_id")
    val tableId: Int,

    val name: String,
    val pitch: String,

    @SerialName("hole_min")
    @ColumnInfo(name = "hole_min")
    val holeMin: String,

    @SerialName("hole_max")
    @ColumnInfo(name = "hole_max")
    val holeMax: String,

    @SerialName("hole_size")
    @ColumnInfo(name = "hole_size")
    val holeSize: String
)