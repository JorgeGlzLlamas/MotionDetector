package com.example.motiondetector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.common.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private lateinit var tvGravedad: TextView
    private lateinit var tvTitulo: TextView

    private lateinit var motionManager: AccelerometerMotionManager
    private lateinit var messageManager: MessageManager

    private val ktorClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvGravedad = findViewById(R.id.tvGravedad)
        tvTitulo = findViewById(R.id.tvTitulo)

        val btnActividades = findViewById<Button>(R.id.btnActividades)
        btnActividades.setOnClickListener {
            val intent = Intent(this, Actividades::class.java)
            startActivity(intent)
        }

        motionManager = AccelerometerMotionManager(this) { event ->
            runOnUiThread {
                updateUI(event)
                updateSimulatedLevelUI(event.gravity)
            }
            Log.d("Mobile", "Evento LOCAL guardado: $event")
            sendEventToTv(event)
        }

        messageManager = MessageManager(this)
        messageManager.setListener { path, msg ->
            when (path) {
                MessagePaths.MOTION_PATH -> {
                    val json = JSONObject(msg)
                    val gravity = json.optString("gravity", "---")
                    val timestampRaw = json.optLong("timestamp", 0L)

                    val timestampText = if (timestampRaw > 0) {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(timestampRaw))
                    } else {
                        "Desconocido"
                    }

                    val event = MotionEventData(
                        source = json.optString("source", "wear"),
                        timestamp = timestampRaw,
                        gravity = gravity,
                        type = json.optString("type", "desconocido")
                    )

                    DataStorage.addEvent(event)

                    runOnUiThread {
                        tvGravedad.text = "Gravedad: $gravity"
                        tvTitulo.text = "Movimiento\nDetectado"
                        updateSimulatedLevelUI(gravity)
                    }
                    Log.d("Mobile", "Evento REMOTO guardado: $event")
                }

                "/evento_simulado" -> {
                    runOnUiThread {
                        val gravedad = when (msg) {
                            "Caida" -> "fuerte"
                            "Correr" -> "medio"
                            "Golpe en mesa" -> "medio-fuerte"
                            else -> "leve"
                        }
                        tvGravedad.text = "Gravedad: $gravedad"
                        tvTitulo.text = "Movimiento detectado en wear:\n $msg"
                        updateSimulatedLevelUI(
                            when (msg) {
                                "Caida" -> "fuerte"
                                "Correr" -> "medio"
                                "Golpe en mesa" -> "medio-fuerte"
                                else -> "leve"
                            }
                        )
                    }
                }
            }
        }

        // ⬇️ NUEVO: Si regresamos desde Actividades con un nivel simulado
        intent.getStringExtra("simulated_level")?.let { level ->
            tvTitulo.text = "Movimiento\nDetectado"
            tvGravedad.text = "Gravedad: $level"
            updateSimulatedLevelUI(level)
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
        tvTitulo.text = "Movimiento\nDetectado"
        tvGravedad.text = "Gravedad: ${event.gravity}"
    }

    fun updateSimulatedLevelUI(level: String) {
        val barraVerde = findViewById<View>(R.id.barraVerde)
        val barraAmarilla = findViewById<View>(R.id.barraAmarilla)
        val barraRoja = findViewById<View>(R.id.barraRoja)

        val paramsVerde = barraVerde.layoutParams as LinearLayout.LayoutParams
        val paramsAmarilla = barraAmarilla.layoutParams as LinearLayout.LayoutParams
        val paramsRoja = barraRoja.layoutParams as LinearLayout.LayoutParams

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

        barraVerde.requestLayout()
        barraAmarilla.requestLayout()
        barraRoja.requestLayout()
    }

    private fun sendEventToTv(event: MotionEventData) {
        val serverUrl = "http://10.0.2.2:8081/motion"
        Log.d("Mobile", "🔗 Enviando evento a URL: $serverUrl")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ktorClient.post(serverUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(event)
                }
                Log.d("Mobile", "Evento enviado a TV: ${response.status}")
            } catch (e: Exception) {
                Log.e("Mobile", "Error enviando evento a TV", e)
            }
        }
    }
}
