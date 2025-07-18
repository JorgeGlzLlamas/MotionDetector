package com.example.common

data class MotionEventData(
    val source: String, // "mobile" o "wear"
    val timestamp: Long,
    val type: String = "significant_motion"
)