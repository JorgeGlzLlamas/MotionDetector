package com.example.motiondetector

import android.util.Log
import com.example.common.MotionEventData
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// Objeto Singleton para gestionar el cliente Ktor
object KtorClient {

    // 1. El cliente se inicializa una sola vez y se reutiliza.
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private const val SERVER_URL = "http://10.0.2.2:8081/motion" // IP para emulador Android

    // 2. Función genérica para enviar eventos desde cualquier parte de la app.
    fun sendEvent(event: MotionEventData) {
        Log.d("KtorClient", "🔗 Enviando evento a URL: $SERVER_URL")

        // Usamos CoroutineScope para lanzar la petición en un hilo de background (IO).
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: HttpResponse = client.post(SERVER_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(event)
                }
                Log.d("KtorClient", "✅ Evento enviado. Status: ${response.status}")
            } catch (e: Exception) {
                // Es importante loguear el error para saber qué falló.
                Log.e("KtorClient", "❌ Error enviando evento a TV", e)
            }
        }
    }
}