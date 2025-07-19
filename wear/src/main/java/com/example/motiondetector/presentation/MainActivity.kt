package com.example.motiondetector.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.common.SignificantMotionManager
import com.example.common.MessagePaths
import org.json.JSONObject
import com.google.android.gms.wearable.Wearable
import com.example.common.MessageManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageManager = MessageManager(this)

        val motionManager = SignificantMotionManager(this) { event ->
            // Crear un JSON para pasar como mensaje a Mobile
            val data = JSONObject().apply {
                put("source", event.source)
                put("timestamp", event.timestamp)
                put("gravity", event.gravity)
                put("type", event.type)
            }

            Wearable.getNodeClient(this).connectedNodes
                .addOnSuccessListener { nodes ->
                    nodes.forEach { node ->
                        messageManager.sendMessage(node.id, MessagePaths.MOTION_PATH, data.toString())
                    }
                }
        }

        motionManager.register()
    }
}