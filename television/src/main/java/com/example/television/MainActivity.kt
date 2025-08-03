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

    private lateinit var tvTitulo: TextView
    private lateinit var tvMagnitud: TextView
    private lateinit var tvTimestamp: TextView
    private lateinit var tvActividad: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("TV", "🟢 Servidor iniciando…")

        // Referencias a vistas
        tvTitulo = findViewById(R.id.tvTituloTV)
        tvMagnitud = findViewById(R.id.tvMagnitud)
        tvTimestamp = findViewById(R.id.tvTimestamp)
        tvActividad = findViewById(R.id.tvActividad)

        // Iniciar servidor y escuchar eventos
        webServer = WebServer { event ->
            Log.d("TV", "🎯 Evento recibido: $event")

            runOnUiThread {
                // Cambiar título si hay evento
                tvTitulo.text = "Movimiento detectado"

                // Mostrar datos del evento
                tvMagnitud.text = "Gravedad: ${event.gravity}"

                val fechaLegible = dateFormatter.format(Date(event.timestamp))
                tvTimestamp.text = "Fecha: $fechaLegible"

                val actividad = event.activity ?: "Desconocida"
                tvActividad.text = "Actividad: $actividad"
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            webServer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TV", "🛑 Servidor detenido")
        webServer.stop()
    }
}
