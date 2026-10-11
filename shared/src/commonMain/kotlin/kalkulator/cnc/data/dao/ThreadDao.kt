package kalkulator.cnc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kalkulator.cnc.entity.ThreadEntity

@Dao
interface ThreadDao {

    @Query("SELECT * FROM threads WHERE category_id = :catId AND table_id = :tabId")
    suspend fun getThreads(catId: Int, tabId: Int): List<ThreadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(threads: List<ThreadEntity>)

    @Query("SELECT COUNT(id) FROM threads")
    suspend fun getThreadsCount(): Int
}