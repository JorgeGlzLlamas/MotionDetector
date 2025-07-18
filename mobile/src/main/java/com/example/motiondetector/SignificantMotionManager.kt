package com.example.motiondetector

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.common.MotionEventData
import kotlin.math.sqrt

class SignificantMotionManager(
    private val context: Context,
    private val onMotionHandled: () -> Unit
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sigMotionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            Log.d("SignificantMotion", "Movimiento detectado por SIGNIFICANT_MOTION")

            captureSingleAccelerometerSample()

            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun register() {
        if (sigMotionSensor == null) {
            Log.w("SignificantMotion", "Sensor TYPE_SIGNIFICANT_MOTION no disponible")
            return
        }
        sensorManager.registerListener(sensorEventListener, sigMotionSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    @SuppressLint("MissingPermission")
    private fun captureSingleAccelerometerSample() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: run {
            handleMotion(0.0)
            return
        }

        val accelListener = object : SensorEventListener {
            @RequiresPermission(Manifest.permission.VIBRATE)
            override fun onSensorChanged(e: SensorEvent) {
                val (x, y, z) = e.values
                val magnitude = sqrt((x * x + y * y + z * z).toDouble())
                sensorManager.unregisterListener(this)
                handleMotion(magnitude)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            accelListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun handleMotion(magnitude: Double) {
        val gravity = when {
            magnitude > 20 -> "fuerte"
            magnitude > 13 -> "medio"
            else -> "leve"
        }

        val event = MotionEventData(
            source = if (context.packageName.contains("wear")) "wear" else "mobile",
            timestamp = System.currentTimeMillis(),
            gravity = gravity
        )

        DataStorage.addEvent(event)
        Log.d("SignificantMotion", "Evento guardado: $event")

        VibrationHandler(context).vibrateBasedOnGravity(gravity)

        onMotionHandled()
    }
}
