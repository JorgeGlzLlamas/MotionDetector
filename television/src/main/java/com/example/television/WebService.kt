package com.example.television

import android.util.Log
import com.example.common.MotionEventData
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

class WebServer(private val onMotionReceived: (MotionEventData) -> Unit) {

    private var server: ApplicationEngine? = null

    fun start() {
        if (server?.application?.isActive == true) {
            Log.d("TV", "🚨 El servidor ya está activo.")
            return
        }

        Log.d("TV", "🚀 Iniciando servidor en puerto 8080...")

        server = embeddedServer(CIO, port = 8080) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                post("/motion") {
                    val event = call.receive<MotionEventData>()
                    Log.d("TV-Server", "📡 Evento recibido en TV: $event")
                    onMotionReceived(event)
                    call.respondText("OK")
                }
            }
        }.start(wait = false)

        Log.d("TV-Server", "✅ Servidor activo en http://0.0.0.0:8080")
    }

    fun stop() {
        Log.d("TV-Server", "⛔ Deteniendo servidor...")
        server?.stop(1000, 2000)
        server = null
    }
}
