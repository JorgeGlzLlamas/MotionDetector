package com.example.common

data class MotionEventData(
    val source: String,
    val timestamp: Long,
    val gravity: String, // leve, medio, fuerte
    val type: String = "significant_motion"
)