package com.hyper.game.space.util

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Window
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ForceFpsAndVsyncEngine {

    /**
     * Finds and applies the closest matching display mode for the requested frame rate.
     * Removes Android's VSync buffer latency on supported OLEDs via preferMinimalPostProcessing.
     */
    suspend fun applyPerformanceDisplayMode(context: Context, window: Window?, targetFps: Float, disableVSync: Boolean) = withContext(Dispatchers.Main) {
        if (window == null) return@withContext
        
        try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            
            // Find the mode that closest matches the target FPS
            val bestMode = display.supportedModes.minByOrNull { Math.abs(it.refreshRate - targetFps) }
            
            val params = window.attributes
            
            bestMode?.let {
                params.preferredDisplayModeId = it.modeId
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && disableVSync) {
                // Bypass display pipeline processing (Auto Low Latency Mode)
                params.preferMinimalPostProcessing = true
            }
            
            window.attributes = params
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
