package kalkulator.cnc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kalkulator.cnc.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: SubscriptionEntity)

    @Query("SELECT * FROM subscription_status WHERE id = 1")
    suspend fun getStatus(): SubscriptionEntity?
    
    @Query("SELECT isPremium FROM subscription_status WHERE id = 1")
    fun observePremiumStatus(): Flow<Boolean?>
}