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
    private val onMotionHandled: (MotionEventData) -> Unit // Callback para cuando detecta movimiento
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var currentLevel = "" // Nivel actual de gravedad detectado (leve, medio, fuerte)
    private var lastUpdateTime = 0L // Tiempo de la última actualización para evitar spam

    // Activa el sensor de acelerómetro
    fun register() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: run {
            Log.w("AccelerometerMotion", "No hay acelerómetro disponible.") // Si no hay sensor
        }
        createNotificationChannel() // Crea canal para notificaciones (Android 8+)
    }

    // Detiene el sensor
    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun onSensorChanged(event: SensorEvent) {
        val (x, y, z) = event.values
        val mag = sqrt(x * x + y * y + z * z) // Calcula magnitud del vector aceleración

        // Clasifica la gravedad según magnitud
        val gravity = when {
            mag > 18 -> "fuerte"
            mag > 14 -> "medio"
            mag > 11 -> "leve"
            else -> ""
        }

        val currentTime = System.currentTimeMillis()

        // Solo actualiza si el nivel cambia y han pasado 3 segundos desde la última vez
        if (gravity != currentLevel && (currentTime - lastUpdateTime > 3000)) {
            currentLevel = gravity
            lastUpdateTime = currentTime

            Log.d("AccelerometerMotion", "📊 Movimiento: $gravity ($mag)")

            if (gravity == "fuerte") {
                vibrate()        // Vibra en caso de movimiento fuerte
                sendNotification() // Envía notificación visual
            }

            // Crea evento con info del movimiento detectado
            val event = MotionEventData(
                source = if (context.packageName.contains("wear")) "wear" else "mobile",
                timestamp = currentTime,
                gravity = gravity
            )

            DataStorage.addEvent(event) // Guarda evento en almacenamiento local
            onMotionHandled(event)      // Llama callback para que la UI o quien sea lo procese
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No usado, pero es obligatorio implementarlo
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 400, 100, 400, 100, 400) // Patrón de vibración

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
            manager.createNotificationChannel(channel) // Crea canal para notificaciones (Android 8+)
        }
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(context, "motion_channel")
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("⚠ Movimiento fuerte detectado")
            .setContentText("Se registró un movimiento intenso")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, builder.build()) // Muestra notificación en pantalla
    }

    // Método para simular movimiento (para pruebas)
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun simulateMotionLevel(level: String, overrideMagnitude: Float? = null) {
        val fakeMag = overrideMagnitude ?: when (level) {
            "fuerte" -> 19f
            "medio" -> 15f
            "leve" -> 12f
            else -> 9f
        }

        val currentTime = System.currentTimeMillis()

        // Solo actualiza si nivel cambia y pasaron más de 3 segundos
        if (level != currentLevel && (currentTime - lastUpdateTime > 3000)) {
            currentLevel = level
            lastUpdateTime = currentTime

            Log.d("AccelerometerMotion", "🎮 Simulado: $level ($fakeMag)")

            if (level == "fuerte") {
                vibrate()
                sendNotification()
            }

            val event = MotionEventData(
                source = if (context.packageName.contains("wear")) "wear" else "mobile",
                timestamp = currentTime,
                gravity = level
            )

            DataStorage.addEvent(event)
            onMotionHandled(event)
        }
    }
}
