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

        // Referencias de views
        tvDeviceStatus = findViewById(com.example.motiondetector.R.id.tvDeviceStatus)
        btnActividades = findViewById(com.example.motiondetector.R.id.btnActividades)

        // Acción del botón para abrir actividades
        btnActividades.setOnClickListener {
            val intent = Intent(this, ActividadesActivity::class.java)
            startActivity(intent)
        }

        // Mostrar nombre del dispositivo conectado
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                if (nodes.isNotEmpty()) {
                    val deviceName = nodes.joinToString { it.displayName }
                    tvDeviceStatus.text = "Conectado a:\n$deviceName"
                } else {
                    tvDeviceStatus.text = "Dispositivo no conectado"
                }
            }
            .addOnFailureListener {
                tvDeviceStatus.text = "Error al verificar conexión"
            }

        // Inicializa los managers
        messageManager = MessageManager(this)

        motionManager = AccelerometerMotionManager(this) { event ->

            val formattedDate = convertTimestampToDate(event.timestamp)

            val data = JSONObject().apply {
                put("source", event.source)
                put("timestamp", event.timestamp) // mantenemos timestamp como Long para Mobile
                put("gravity", event.gravity)
                put("type", event.type)
            }

            // Enviar evento a nodos conectados (ej. móvil)
            Wearable.getNodeClient(this).connectedNodes
                .addOnSuccessListener { nodes ->
                    nodes.forEach { node ->
                        messageManager.sendMessage(node.id, MessagePaths.MOTION_PATH, data.toString())
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

    private fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
