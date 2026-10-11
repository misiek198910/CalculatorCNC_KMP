package kalkulator.cnc.entity

import kotlinx.serialization.Serializable

@Serializable
data class NewsItem(
    val title: String = "",
    val content: String = "",
    val timestamp: Long? = null, // Zamiennik dla java.util.Date
    val isVisible: Boolean = true,
    val actionLink: String? = null,
    val imageUrl: String? = null
)