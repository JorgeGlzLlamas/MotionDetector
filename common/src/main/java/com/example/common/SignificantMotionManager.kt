package com.example.common

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.*
import android.util.Log
import kotlin.math.sqrt
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.annotation.RequiresPermission
import kotlin.math.sqrt

class SignificantMotionManager(
    private val context: Context,
    private val onMotionHandled: (MotionEventData) -> Unit
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val triggerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val triggerListener = object : TriggerEventListener() {
        @RequiresPermission(Manifest.permission.VIBRATE)
        override fun onTrigger(event: TriggerEvent?) {
            Log.d("SignificantMotion", "✅ Movimiento detectado (TRIGGER)")
            handleMotion(15.0) // Valor aproximado ya que el trigger no da magnitud
        }
    }

    private val accelListener = object : SensorEventListener {
        @RequiresPermission(Manifest.permission.VIBRATE)
        override fun onSensorChanged(e: SensorEvent) {
            val (x, y, z) = e.values
            val magnitude = sqrt((x * x + y * y + z * z).toDouble())
            sensorManager.unregisterListener(this)
            Log.d("SignificantMotion", "✅ Movimiento detectado (ACCELEROMETER): $magnitude")
            handleMotion(magnitude)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun register() {
        when {
            triggerSensor != null -> {
                Log.d("SignificantMotion", "🟢 Registrando sensor de tipo TRIGGER")
                sensorManager.requestTriggerSensor(triggerListener, triggerSensor)
            }

            accelerometer != null -> {
                Log.d("SignificantMotion", "🟡 TRIGGER no disponible, usando acelerómetro")
                sensorManager.registerListener(
                    accelListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }

            else -> {
                Log.w("SignificantMotion", "🔴 Ningún sensor disponible para detectar movimiento")
            }
        }
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
        Log.d("SignificantMotion", "💾 Evento guardado: $event")

        VibrationHandler(context).vibrateBasedOnGravity(gravity)

        onMotionHandled(event)
    }
}