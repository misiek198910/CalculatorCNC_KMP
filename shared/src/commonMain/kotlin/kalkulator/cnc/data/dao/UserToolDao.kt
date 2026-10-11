package kalkulator.cnc.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kalkulator.cnc.entity.UserToolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserToolDao {
    @Query("SELECT * FROM user_tools ORDER BY id DESC")
    fun getAllTools(): Flow<List<UserToolEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: UserToolEntity)

    @Update
    suspend fun updateTool(tool: UserToolEntity)

    @Delete
    suspend fun deleteTool(tool: UserToolEntity)

    @Query("SELECT * FROM user_tools WHERE id = :id")
    suspend fun getToolById(id: Int): UserToolEntity?
}