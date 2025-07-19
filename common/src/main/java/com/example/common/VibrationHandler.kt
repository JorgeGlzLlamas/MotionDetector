package com.example.common

import android.Manifest
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresPermission

class VibrationHandler(private val context: Context) {

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrateBasedOnGravity(gravity: String) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return

        val (duration, amplitude) = when (gravity) {
            "fuerte" -> 600L to 255
            "medio" -> 300L to 180
            "leve"  -> 100L to 80
            else    -> return
        }

        val effect = VibrationEffect.createOneShot(duration, amplitude)
        vibrator.vibrate(effect)
    }
}