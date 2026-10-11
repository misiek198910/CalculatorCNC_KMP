package kalkulator.cnc.remote

import kotlinx.serialization.Serializable

@Serializable
data class PrivacyPolicyResponse(
    val service: String,
    val content: String
)