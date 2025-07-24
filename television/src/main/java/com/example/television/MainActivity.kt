package com.example.television

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {

    private lateinit var webServer: WebServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("TV", "🟢 onCreate iniciado, preparando servidor...")

        webServer = WebServer { event ->
            Log.d("TV", "🎯 Evento procesado: $event")
        }

        lifecycleScope.launch(Dispatchers.IO) {
            webServer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TV", "🛑 onDestroy: Deteniendo servidor...")
        webServer.stop()
    }
}
