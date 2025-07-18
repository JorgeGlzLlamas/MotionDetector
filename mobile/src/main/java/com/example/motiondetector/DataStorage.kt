package com.example.motiondetector

import com.example.common.MotionEventData


object DataStorage {
    val motionEvents = mutableListOf<MotionEventData>()

    fun addEvent(event: MotionEventData) {
        motionEvents.add(0, event) // Insertar al inicio (más reciente primero)
    }

    fun getAllEvents(): List<MotionEventData> = motionEvents
}