package kalkulator.cnc.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CncRecord(
    @SerialName("articleNumber") val articleNumber: String? = null,
    @SerialName("toolCategory") val toolCategory: String? = null,
    @SerialName("toolModelName") val toolModelName: String? = null,
    @SerialName("materialGroup") val materialGroup: String? = null,
    @SerialName("diameter") val diameter: Double? = null,
    @SerialName("flutesCount") val flutesCount: Int? = null,
    @SerialName("vcMin") val vcMin: Double? = null,
    @SerialName("vcMax") val vcMax: Double? = null,
    @SerialName("feedStep") val feedStep: Double? = null,
    @SerialName("category_code")
    val categoryCode: String? = null

)