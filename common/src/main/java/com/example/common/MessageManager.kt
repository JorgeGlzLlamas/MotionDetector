package com.example.common

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.Wearable

class MessageManager(private val context: Context) {

    private val messageClient: MessageClient by lazy {
        Wearable.getMessageClient(context)
    }

    // Envía un mensaje al nodo (wear o móvil)
    fun sendMessage(nodeId: String, path: String, message: String) {
        messageClient.sendMessage(nodeId, path, message.toByteArray())
            .addOnSuccessListener {
                Log.d("MessageManager", "✅ Mensaje enviado a $nodeId por $path")
            }
            .addOnFailureListener {
                Log.e("MessageManager", "❌ Error al enviar mensaje", it)
            }
    }

    // Escucha mensajes entrantes
    fun setListener(onMessageReceived: (path: String, message: String) -> Unit) {
        messageClient.addListener(object : OnMessageReceivedListener {
            override fun onMessageReceived(event: MessageEvent) {
                val path = event.path
                val msg = String(event.data)
                Log.d("MessageManager", "📩 Mensaje recibido en $path: $msg")
                onMessageReceived(path, msg)
            }
        })
    }
}