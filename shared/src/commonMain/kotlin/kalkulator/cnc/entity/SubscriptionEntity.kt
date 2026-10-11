package kalkulator.cnc.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscription_status")
data class SubscriptionEntity(
    @PrimaryKey val id: Int = 1, // Zawsze jeden wiersz w tabeli nadpisywany nowym statusem
    val isPremium: Boolean,
    val purchaseToken: String? = null
)