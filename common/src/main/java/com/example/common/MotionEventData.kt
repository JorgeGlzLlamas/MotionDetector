package com.example.common

import kotlinx.serialization.Serializable

@Serializable
data class MotionEventData(
    val source: String,
    val timestamp: Long,
    val gravity: String,
    val type: String,
    val activity: String? = null
)
