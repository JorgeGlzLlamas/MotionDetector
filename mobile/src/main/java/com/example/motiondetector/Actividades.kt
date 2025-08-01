package com.example.motiondetector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.common.AccelerometerMotionManager

class Actividades : AppCompatActivity() {

    private lateinit var motionManager: AccelerometerMotionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Activa modo edge-to-edge para mejor UI
        setContentView(R.layout.activity_actividades) // Carga el layout de actividades

        // Inicializa el gestor de movimiento con callback para loguear evento simulado
        motionManager = AccelerometerMotionManager(this) { event ->
            Log.d("Actividades", "Evento simulado: ${event.gravity}")
        }
        motionManager.register()  // Registra el listener del sensor acelerómetro

        // Configura los botones para simular distintos niveles de movimiento
        findViewById<LinearLayout>(R.id.btnCaminar).setOnClickListener {
            motionManager.simulateMotionLevel("leve") // Simula movimiento leve
            sendLevelToMain("leve") // Envía nivel simulado a MainActivity
        }

        findViewById<LinearLayout>(R.id.btnCorrer).setOnClickListener {
            motionManager.simulateMotionLevel("medio") // Simula movimiento medio
            sendLevelToMain("medio") // Envía nivel simulado a MainActivity
        }

        findViewById<LinearLayout>(R.id.btnSaltar).setOnClickListener {
            motionManager.simulateMotionLevel("medio", overrideMagnitude = 16.5f) // Simula salto con magnitud media-fuerte
            sendLevelToMain("medio") // Envía nivel medio-fuerte a MainActivity
        }

        findViewById<LinearLayout>(R.id.btnEscaleras).setOnClickListener {
            motionManager.simulateMotionLevel("leve", overrideMagnitude = 13.5f) // Simula subir escaleras con magnitud leve-medio
            sendLevelToMain("leve") // Envía nivel leve-medio a MainActivity
        }

        findViewById<LinearLayout>(R.id.btnCaida).setOnClickListener {
            motionManager.simulateMotionLevel("fuerte") // Simula movimiento fuerte (caída)
            sendLevelToMain("fuerte") // Envía nivel fuerte a MainActivity
        }
    }

    // Función para mandar intent a MainActivity con el nivel simulado y cerrar esta actividad
    private fun sendLevelToMain(level: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("simulated_level", level)
        startActivity(intent)
        finish() // Evita acumular actividades en el backstack
    }

    override fun onDestroy() {
        super.onDestroy()
        motionManager.unregister() // Libera el sensor cuando se destruye la actividad
    }
}
