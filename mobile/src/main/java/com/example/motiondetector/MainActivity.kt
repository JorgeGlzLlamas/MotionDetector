package com.example.motiondetector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.view.View
import android.widget.LinearLayout
import com.example.common.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*

class MainActivity : ComponentActivity() {

    private lateinit var tvGravedad: TextView
    private lateinit var tvTimestamp: TextView

    private lateinit var motionManager: AccelerometerMotionManager
    private lateinit var messageManager: MessageManager

    // Cliente Ktor para enviar datos al servidor
    private val ktorClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Botón para abrir actividad Actividades
        val btnActividades = findViewById<Button>(R.id.btnActividades)
        btnActividades.setOnClickListener {
            val intent = Intent(this, Actividades::class.java)
            startActivity(intent)
        }

        // Referencias a TextViews de gravedad y timestamp
        tvGravedad = findViewById(R.id.tvGravedad)
        tvTimestamp = findViewById(R.id.tvTimestamp)

        // Inicializa el detector de movimiento con callback para actualizar UI y enviar evento
        motionManager = AccelerometerMotionManager(this) { event ->
            runOnUiThread {
                updateUI(event) // Actualiza textos en pantalla
                updateSimulatedLevelUI(event.gravity) // Actualiza barra visual según gravedad
            }
            Log.d("Mobile", "Evento LOCAL guardado: $event")
            KtorClient.sendEvent(event) // Envía evento al servidor (TV)
        }

        // Si la actividad fue llamada con un nivel simulado, actualiza la UI con ese nivel
        intent.getStringExtra("simulated_level")?.let {
            updateSimulatedLevelUI(it)
            tvGravedad.text = "Gravity: $it"
            tvTimestamp.text = "Timestamp: Simulado"
        }

        // Inicializa messageManager para escuchar eventos remotos (ej. de Wear OS)
        messageManager = MessageManager(this)
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
                runOnUiThread {
                    updateUI(event)
                    updateSimulatedLevelUI(event.gravity)
                }
                Log.d("Mobile", "Evento REMOTO guardado: $event")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        motionManager.register() // Activa sensor al reanudar actividad
    }

    override fun onPause() {
        super.onPause()
        motionManager.unregister() // Detiene sensor al pausar actividad
    }

    // Actualiza textos con datos de evento
    private fun updateUI(event: MotionEventData) {
        tvTimestamp.text = "Timestamp: ${event.timestamp}"
        tvGravedad.text = "Gravity: ${event.gravity}"
    }

    // Actualiza la barra de gravedad según nivel simulado o real
    fun updateSimulatedLevelUI(level: String) {
        val barraVerde = findViewById<View>(R.id.barraVerde)
        val barraAmarilla = findViewById<View>(R.id.barraAmarilla)
        val barraRoja = findViewById<View>(R.id.barraRoja)

        val paramsVerde = barraVerde.layoutParams as LinearLayout.LayoutParams
        val paramsAmarilla = barraAmarilla.layoutParams as LinearLayout.LayoutParams
        val paramsRoja = barraRoja.layoutParams as LinearLayout.LayoutParams

        // Ajusta el peso de cada barra según el nivel recibido
        when (level) {
            "leve" -> {
                paramsVerde.weight = 3f
                paramsAmarilla.weight = 1f
                paramsRoja.weight = 1f
            }
            "medio", "leve-medio" -> {
                paramsVerde.weight = 1f
                paramsAmarilla.weight = 3f
                paramsRoja.weight = 1f
            }
            "medio-fuerte" -> {
                paramsVerde.weight = 1f
                paramsAmarilla.weight = 2f
                paramsRoja.weight = 2f
            }
            "fuerte" -> {
                paramsVerde.weight = 1f
                paramsAmarilla.weight = 1f
                paramsRoja.weight = 3f
            }
            else -> {
                paramsVerde.weight = 1f
                paramsAmarilla.weight = 1f
                paramsRoja.weight = 1f
            }
        }

        barraVerde.layoutParams = paramsVerde
        barraAmarilla.layoutParams = paramsAmarilla
        barraRoja.layoutParams = paramsRoja

        // Pide refrescar la UI para aplicar los cambios de peso
        barraVerde.requestLayout()
        barraAmarilla.requestLayout()
        barraRoja.requestLayout()
    }
}