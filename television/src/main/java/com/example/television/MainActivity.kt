package com.example.television

//import android.os.Bundle
//import androidx.activity.ComponentActivity
// androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.tv.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.RectangleShape
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.tv.material3.ExperimentalTvMaterial3Api
//import androidx.tv.material3.Surface
//import com.example.television.ui.theme.MotionDetectorTheme
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.motiondetector.MessageManager
import com.example.common.MessagePaths

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageManager = MessageManager(this)

        messageManager.setListener { path, msg ->
            if (path == MessagePaths.MOTION_PATH) {
                Log.d("TV", "Alerta de movimiento: $msg")
                // Aquí puedes actualizar UI con datos del móvil
            }
        }
    }
}