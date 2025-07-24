package com.example.motiondetector

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
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

    // ⬇️ Cliente Ktor configurado para enviar eventos
    private val ktorClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

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

            // ⬇️ Añadir llamada para enviar evento a la TV
            sendEventToTv(event)
        }
        val simulatedEvent = MotionEventData(
            source = "mobile-test",
            timestamp = System.currentTimeMillis(),
            gravity = "[0.0, 9.8, 0.0]",
            type = "TEST"
        )

        Log.d("Mobile", "🧪 Enviando evento simulado a TV: $simulatedEvent")
        sendEventToTv(simulatedEvent)

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

    // ⬇️ NUEVA función para enviar el evento al servidor (TV)
    private fun sendEventToTv(event: MotionEventData) {
        val serverUrl = "http://10.0.2.2:8081/motion"
        Log.d("Mobile", "🔗 Enviando evento a URL: $serverUrl")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("Mobile", "⏳ Enviando evento a TV...")
                val response: HttpResponse = ktorClient.post(serverUrl) {
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
