package com.example.motiondetector

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.common.*
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private lateinit var tvGravedad: TextView
    private lateinit var tvTimestamp: TextView

    private lateinit var motionManager: AccelerometerMotionManager
    private lateinit var messageManager: MessageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvGravedad = findViewById(R.id.tvGravedad)
        tvTimestamp = findViewById(R.id.tvTimestamp)

        messageManager = MessageManager(this)

        // ⏬ NUEVA implementación con solo acelerómetro
        motionManager = AccelerometerMotionManager(this) { event ->
            runOnUiThread { updateUI(event) }
            Log.d("Mobile", "Evento LOCAL guardado: $event")
        }

        // Escuchar eventos remotos (del Wear)
        messageManager.setListener { path, msg ->
            if (path == MessagePaths.MOTION_PATH) {
                val json = JSONObject(msg)
                val event = MotionEventData(
                    source = json.getString("source"),
                    timestamp = json.getLong("timestamp"),
                    gravity = json.getString("gravity"),
                    type = json.getString("type")
                )
                DataStorage.addEvent(event)
                runOnUiThread { updateUI(event) }
                Log.d("Mobile", "Evento REMOTO guardado: $event")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        motionManager.register()
    }

    override fun onPause() {
        super.onPause()
        motionManager.unregister()
    }

    private fun updateUI(event: MotionEventData) {
        tvTimestamp.text = "Timestamp: ${event.timestamp}"
        tvGravedad.text = "Gravity: ${event.gravity}"
    }
}
