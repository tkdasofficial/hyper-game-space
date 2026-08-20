package com.hyper.game.space.util

import android.os.Build
import android.view.Window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VSyncFramePacingManager {
    
    /**
     * Configure low-latency frame pacing and minimal post-processing (API 30+).
     * This bypasses excessive display pipeline buffers on supported TVs and external displays,
     * as well as built-in OLED panels supporting low latency game modes.
     */
    suspend fun optimizeFramePacing(window: Window, enableMinimalPostProcessing: Boolean) = withContext(Dispatchers.Main) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val params = window.attributes
                params.preferMinimalPostProcessing = enableMinimalPostProcessing
                window.attributes = params
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
