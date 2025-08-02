package com.example.motiondetector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.common.AccelerometerMotionManager
import com.example.common.MotionEventData

class Actividades : AppCompatActivity() {

    private lateinit var motionManager: AccelerometerMotionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_actividades)

        motionManager = AccelerometerMotionManager(this) { event ->
            Log.d("Actividades", "Evento simulado (local): ${event.gravity}")
        }
        motionManager.register()

        findViewById<LinearLayout>(R.id.btnCaminar).setOnClickListener {
            sendEventToTv("leve", "Caminar")
            sendLevelToMain("leve")
        }

        findViewById<LinearLayout>(R.id.btnCorrer).setOnClickListener {
            sendEventToTv("medio", "Correr")
            sendLevelToMain("medio")
        }

        findViewById<LinearLayout>(R.id.btnSaltar).setOnClickListener {
            sendEventToTv("medio-fuerte", "Saltar")
            sendLevelToMain("medio")
        }

        findViewById<LinearLayout>(R.id.btnEscaleras).setOnClickListener {
            sendEventToTv("leve-medio", "Subir escaleras")
            sendLevelToMain("leve")
        }

        findViewById<LinearLayout>(R.id.btnCaida).setOnClickListener {
            sendEventToTv("fuerte", "Caída")
            sendLevelToMain("fuerte")
        }
    }

    private fun sendEventToTv(gravityLevel: String, activityName: String) {
        val simulatedEvent = MotionEventData(
            source = "Mobile-Simulation",
            timestamp = System.currentTimeMillis(),
            gravity = gravityLevel,
            type = "simulation",
            activity = activityName
        )
        Log.d("Actividades", "Enviando evento simulado a la TV: $simulatedEvent")
        KtorClient.sendEvent(simulatedEvent)
    }

    private fun sendLevelToMain(level: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("simulated_level", level)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        motionManager.unregister()
    }
}
