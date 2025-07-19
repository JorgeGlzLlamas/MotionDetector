package com.example.motiondetector

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.common.MessagePaths
import com.example.common.MotionEventData
import com.example.common.MessageManager
import com.example.common.SignificantMotionManager
import com.example.common.DataStorage
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageManager = MessageManager(this)

        // Detectar movimiento propio
        val motionManager = SignificantMotionManager(this) { event ->
            // Obtener los datos del evento
            Log.d("Mobile", "Evento guardado: $event")
        }

        motionManager.register()

        // Escuchar eventos del Wear
        messageManager.setListener { path, msg ->
            if (path == MessagePaths.MOTION_PATH) {
                // Obtener el mensaje JSON de Wear
                val json = JSONObject(msg)
                // Crear un evento en mobile a partir de los datos de Wear
                val event = MotionEventData(
                    source = json.getString("source"),
                    timestamp = json.getLong("timestamp"),
                    gravity = json.getString("gravity"),
                    type = json.getString("type")
                )
                DataStorage.addEvent(event)
                Log.d("Mobile", "Evento REMOTO guardado: $event")
            }
        }
    }
}