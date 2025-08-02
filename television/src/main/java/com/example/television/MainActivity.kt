package com.example.television

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var webServer: WebServer

    private val dateFormatter = SimpleDateFormat("HH:mm:ss  dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("TV", "🟢 Servidor iniciando…")

        webServer = WebServer { event ->
            Log.d("TV", "🎯 Evento recibido: $event")

            runOnUiThread {
                findViewById<TextView>(R.id.tvMagnitud).text =
                    "Gravedad: ${event.gravity}"

                val fechaLegible = dateFormatter.format(Date(event.timestamp))
                findViewById<TextView>(R.id.tvTimestamp).text =
                    "Fecha: $fechaLegible"

                val actividad = event.activity ?: "Desconocida"
                findViewById<TextView>(R.id.tvActividad).text =
                    "Actividad: $actividad"
            }
        }

        lifecycleScope.launch(Dispatchers.IO) { webServer.start() }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TV", "🛑 Servidor detenido")
        webServer.stop()
    }
}
