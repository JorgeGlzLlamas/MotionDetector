package com.example.motiondetector.presentation

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.common.MessageManager
import com.example.motiondetector.R
import com.google.android.gms.wearable.Wearable // ✅ IMPORT CORRECTO

class ActividadesActivity : ComponentActivity() {

    private lateinit var messageManager: MessageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades)

        messageManager = MessageManager(applicationContext)

        // Referencias a los botones del layout
        val btnSimularCaida = findViewById<Button>(R.id.btnSimularCaida)
        val btnCorrer = findViewById<Button>(R.id.btnCorrer)
        val btnGolpearMesa = findViewById<Button>(R.id.btnGolpearMesa)

        // Eventos simulados
        btnSimularCaida.setOnClickListener {
            sendSimulatedEvent("Nivel medio")
        }

        btnCorrer.setOnClickListener {
            sendSimulatedEvent("Nivel leve")
        }

        btnGolpearMesa.setOnClickListener {
            sendSimulatedEvent("Nivel fuerte")
        }
    }

    private fun sendSimulatedEvent(evento: String) {
        val path = "/evento_simulado"
        Wearable.getNodeClient(applicationContext).connectedNodes
            .addOnSuccessListener { nodes ->
                for (node in nodes) {
                    messageManager.sendMessage(node.id, path, evento)
                }
            }
    }
}
