package com.example.common

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt

class AccelerometerMotionManager(
    private val context: Context,
    private val onMotionHandled: (MotionEventData) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var currentLevel = ""
    private var lastUpdateTime = 0L

    fun register() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: run {
            Log.w("AccelerometerMotion", "No hay acelerómetro disponible.")
        }
        createNotificationChannel()
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun onSensorChanged(event: SensorEvent) {
        val (x, y, z) = event.values
        val mag = sqrt(x * x + y * y + z * z)

        val gravity = when {
            mag > 18 -> "fuerte"
            mag > 14 -> "medio"
            mag > 11 -> "leve"
            else -> ""
        }

        val currentTime = System.currentTimeMillis()

        if (gravity != currentLevel && (currentTime - lastUpdateTime > 3000)) {
            currentLevel = gravity
            lastUpdateTime = currentTime

            Log.d("AccelerometerMotion", "📊 Movimiento: $gravity ($mag)")

            // Vibrar y notificar si es fuerte
            if (gravity == "fuerte") {
                vibrate()
                sendNotification()
            }

            val event = MotionEventData(
                source = if (context.packageName.contains("wear")) "wear" else "mobile",
                timestamp = System.currentTimeMillis(),
                gravity = gravity
            )

            DataStorage.addEvent(event)
            onMotionHandled(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No hace falta usar esto
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 400, 100, 400, 100, 400)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(pattern, -1)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "motion_channel",
                "Movimiento fuerte",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(context, "motion_channel")
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("⚠ Movimiento fuerte detectado")
            .setContentText("Se registró un movimiento intenso")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, builder.build())
    }
}
