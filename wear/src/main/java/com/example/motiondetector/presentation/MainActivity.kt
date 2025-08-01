package com.example.motiondetector.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.common.*
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var motionManager: AccelerometerMotionManager
    private lateinit var messageManager: MessageManager
    private lateinit var tvDeviceStatus: TextView
    private lateinit var btnActividades: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.motiondetector.R.layout.activity_main)

        // Referencias de vistas
        tvDeviceStatus = findViewById(com.example.motiondetector.R.id.tvDeviceStatus)
        btnActividades = findViewById(com.example.motiondetector.R.id.btnActividades)

        // 1️⃣ Mostrar el nombre del dispositivo móvil conectado
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                if (nodes.isNotEmpty()) {
                    val nodeName = nodes.first().displayName
                    tvDeviceStatus.text = "Conectado a: $nodeName"
                } else {
                    tvDeviceStatus.text = "Dispositivo no conectado"
                }
            }
            .addOnFailureListener {
                tvDeviceStatus.text = "Error al verificar conexión"
            }

        // 2️⃣ Acción del botón para abrir actividades
        btnActividades.setOnClickListener {
            val intent = Intent(this, ActividadesActivity::class.java)
            startActivity(intent)
        }

        // 3️⃣ Inicializa los managers
        messageManager = MessageManager(this)
        motionManager = AccelerometerMotionManager(this) { event ->
            // Convertimos el timestamp a fecha legible
            val readableDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(Date(event.timestamp))

            // Creamos el JSON del evento
            val data = JSONObject().apply {
                put("source", event.source)
                put("timestamp", readableDate) // Enviamos la fecha legible ya convertida
                put("gravity", event.gravity)
                put("type", event.type)
            }

            // Enviamos el mensaje a todos los nodos conectados
            Wearable.getNodeClient(this).connectedNodes
                .addOnSuccessListener { nodes ->
                    nodes.forEach { node ->
                        messageManager.sendMessage(
                            node.id,
                            MessagePaths.MOTION_PATH,
                            data.toString()
                        )
                    }
                }
        }

        motionManager.register()
    }

    override fun onResume() {
        super.onResume()
        motionManager.register()
    }

    override fun onPause() {
        super.onPause()
        motionManager.unregister()
    }
}
