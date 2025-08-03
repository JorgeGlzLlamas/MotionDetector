package com.example.common

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageManager(private val context: Context) {

    private val messageClient: MessageClient by lazy {
        Wearable.getMessageClient(context)
    }

    // Envía mensaje a nodo (wear o móvil)
    fun sendMessage(nodeId: String, path: String, message: String) {
        messageClient.sendMessage(nodeId, path, message.toByteArray())
            .addOnSuccessListener {
                Log.d("MessageManager", "✅ Mensaje enviado a $nodeId por $path")
            }
            .addOnFailureListener {
                Log.e("MessageManager", "❌ Error al enviar mensaje", it)
            }
    }

    // Escucha mensajes entrantes y llama callback en hilo IO
    fun setListener(onMessageReceived: (path: String, message: String) -> Unit) {
        messageClient.addListener(object : OnMessageReceivedListener {
            override fun onMessageReceived(event: MessageEvent) {
                val path = event.path
                val msg = String(event.data)
                Log.d("MessageManager", "📩 Mensaje recibido en $path: $msg")

                CoroutineScope(Dispatchers.IO).launch {
                    onMessageReceived(path, msg)
                }
            }
        })
    }
}
