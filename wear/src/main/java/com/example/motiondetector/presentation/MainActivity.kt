package com.example.motiondetector.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.common.*
import com.example.common.MessagePaths
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private lateinit var motionManager: AccelerometerMotionManager
    private lateinit var messageManager: MessageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        messageManager = MessageManager(this)

        motionManager = AccelerometerMotionManager(this) { event ->
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

    override fun onResume() {
        super.onResume()
        motionManager.register()
    }

    override fun onPause() {
        super.onPause()
        motionManager.unregister()
    }
}
