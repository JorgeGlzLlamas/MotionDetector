package com.example.motiondetector

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.motiondetector.MessageManager
import com.example.common.MessagePaths
import com.example.motiondetector.SignificantMotionManager
import com.example.common.MotionEventData
import org.json.JSONObject
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageManager = MessageManager(this)

        // Detectar movimiento propio
        val motionManager = SignificantMotionManager(this) {
            val event = MotionEventData(
                source = "mobile",
                timestamp = System.currentTimeMillis()
            )
            DataStorage.addEvent(event)
            Log.d("Mobile", "Evento LOCAL guardado: $event")
        }

        motionManager.register()

        // Escuchar eventos del Wear
        messageManager.setListener { path, msg ->
            if (path == MessagePaths.MOTION_PATH) {
                val json = JSONObject(msg)
                val event = MotionEventData(
                    source = json.getString("source"),
                    timestamp = json.getLong("timestamp"),
                    type = json.getString("type")
                )
                DataStorage.addEvent(event)
                Log.d("Mobile", "Evento REMOTO guardado: $event")
            }
        }
    }
}