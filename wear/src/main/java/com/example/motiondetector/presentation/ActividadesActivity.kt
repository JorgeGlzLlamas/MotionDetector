package com.example.motiondetector.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.common.MessageManager
import com.example.motiondetector.R
import com.google.android.gms.wearable.Wearable

class ActividadesActivity : ComponentActivity() {

    private lateinit var messageManager: MessageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades)  // Tu XML con ScrollView

        messageManager = MessageManager(applicationContext)

        // Referencias a los botones de tu layout
        val btnSimularCaida = findViewById<Button>(R.id.btnSimularCaida)
        val btnCorrer = findViewById<Button>(R.id.btnCorrer)
        val btnGolpearMesa = findViewById<Button>(R.id.btnGolpearMesa)

        btnSimularCaida.setOnClickListener {
            sendSimulatedEvent("Caida")
        }

        btnCorrer.setOnClickListener {
            sendSimulatedEvent("Correr")
        }

        btnGolpearMesa.setOnClickListener {
            sendSimulatedEvent("Golpe en mesa")
        }
    }

    private fun sendSimulatedEvent(evento: String) {
        val path = "/evento_simulado"
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                if (nodes.isEmpty()) {
                    Log.w("ActividadesActivity", "No hay nodos conectados para enviar mensaje")
                }
                for (node in nodes) {
                    messageManager.sendMessage(node.id, path, evento)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ActividadesActivity", "Error obteniendo nodos conectados", e)
            }
    }
}
