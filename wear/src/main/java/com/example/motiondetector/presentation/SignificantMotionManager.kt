package com.example.motiondetector.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import android.util.Log

class SignificantMotionManager(
    private val context: Context,
    private val onMotionDetected: () -> Unit
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val motionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)

    private val triggerListener = object : TriggerEventListener() {
        override fun onTrigger(event: TriggerEvent?) {
            Log.d("SignificantMotion", "Movimiento detectado")
            onMotionDetected()
            // No es necesario removerlo, se desactiva solo
        }
    }

    fun register() {
        if (motionSensor == null) {
            Log.w("SignificantMotion", "Sensor no disponible")
            return
        }

        sensorManager.requestTriggerSensor(triggerListener, motionSensor)
    }
}