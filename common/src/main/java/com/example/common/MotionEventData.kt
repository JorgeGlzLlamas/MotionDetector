package com.example.common

import kotlinx.serialization.Serializable

@Serializable
data class MotionEventData(
    val source: String,
    val timestamp: Long,
    val gravity: String, // leve, medio, fuerte
    val type: String = "significant_motion"
)