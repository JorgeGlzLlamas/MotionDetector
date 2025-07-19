package com.example.television

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.common.MessageManager
import com.example.common.MessagePaths


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val messageManager = MessageManager(this)

        messageManager.setListener { path, msg ->
            if (path == MessagePaths.MOTION_PATH) {
                Log.d("TV", "Alerta de movimiento: $msg")
                // Aquí puedes actualizar UI con datos del móvil
            }
        }
    }
}