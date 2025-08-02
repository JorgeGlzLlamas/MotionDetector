package com.example.motiondetector.presentation

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.common.MessageManager
import com.example.motiondetector.R

class ActividadesActivity : ComponentActivity() {

    private lateinit var messageManager: MessageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades)

        messageManager = MessageManager(applicationContext)

        // Simular caída
        val btnSimularCaida = findViewById<Button>(R.id.btnSimularCaida)
        btnSimularCaida.setOnClickListener {
            sendSimulatedEvent("caida_simulada")
        }

        // Correr
        val btnCorrer = findViewById<Button>(R.id.btnCorrer)
        btnCorrer.setOnClickListener {
            sendSimulatedEvent("correr_simulado")
        }

        // Golpear la mesa
        val btnGolpearMesa = findViewById<Button>(R.id.btnGolpearMesa)
        btnGolpearMesa.setOnClickListener {
            sendSimulatedEvent("golpe_mesa_simulado")
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
