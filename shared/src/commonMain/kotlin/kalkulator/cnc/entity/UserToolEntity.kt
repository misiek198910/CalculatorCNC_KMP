package kalkulator.cnc.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_tools")
data class UserToolEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workpiece: String? = null,
    val name: String,
    val f: String? = null,
    val s: String? = null,
    val notes: String? = null
)