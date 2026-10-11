package kalkulator.cnc.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("image_url") val image_url: String? = null,
    @SerialName("action_link") val action_link: String? = null,
    @SerialName("publish_date") val publish_date: String? = null,
    @SerialName("is_visible") val is_visible: Boolean? = false
)