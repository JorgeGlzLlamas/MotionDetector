package com.example.motiondetector

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.common.MessagePaths
import com.example.common.MotionEventData
import com.example.common.MessageManager
import com.example.common.SignificantMotionManager
import com.example.common.DataStorage
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    // Declarar las variables como propiedades de la clase
    private lateinit var tvGravedad: TextView
    private lateinit var tvTimestamp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Renderizar el layout
        setContentView(R.layout.activity_main)

        // Obtener elementos de la UI
        tvGravedad = findViewById<TextView>(R.id.tvGravedad)
        tvTimestamp = findViewById<TextView>(R.id.tvTimestamp)

        val messageManager = MessageManager(this)

        // Detectar movimiento propio
        val motionManager = SignificantMotionManager(this) { event ->
            // Mostrar datos en los TextView
            runOnUiThread {
                updateUI(event)
            }
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

                runOnUiThread {
                    updateUI(event)
                }
                Log.d("Mobile", "Evento REMOTO guardado: $event")
            }
        }
    }

    // Creamos función para asignar los valores del evento a la UI
    private fun updateUI(event: MotionEventData) {
        tvTimestamp.text = "Timestamp: ${event.timestamp}"
        tvGravedad.text = "Gravity: ${event.gravity}"
    }
}