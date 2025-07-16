/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.motiondetector.presentation

//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.wear.compose.material.MaterialTheme
//import androidx.wear.compose.material.Text
//import androidx.wear.compose.material.TimeText
// import androidx.wear.tooling.preview.devices.WearDevices
//import com.example.motiondetector.R
//import com.example.motiondetector.presentation.theme.MotionDetectorTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.motiondetector.SignificantMotionManager
import com.example.motiondetector.presentation.MessageManager
import com.example.common.MessagePaths
import org.json.JSONObject
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageManager = MessageManager(this)

        val motionManager = SignificantMotionManager(this) {
            val data = JSONObject().apply {
                put("source", "wear")
                put("timestamp", System.currentTimeMillis())
                put("type", "significant_motion")
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