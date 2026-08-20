package com.example.util

import android.content.Context
import android.hardware.display.DisplayManager
import android.provider.Settings
import android.view.Display
import android.view.Window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ForceFpsManager {
    
    /**
     * Query supported display refresh rates from the device.
     */
    suspend fun getSupportedRefreshRates(context: Context): List<Float> = withContext(Dispatchers.IO) {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        display.supportedModes.map { it.refreshRate }.distinct().sorted()
    }

    /**
     * Set the preferred refresh rate directly to a targeted Window.
     * Useful if creating a game overlay or custom surface.
     */
    fun applyFpsLockToWindow(window: Window, targetFps: Float) {
        val display = window.windowManager.defaultDisplay
        val modes = display.supportedModes
        val bestMode = modes.minByOrNull { Math.abs(it.refreshRate - targetFps) }
        
        bestMode?.let { mode ->
            val params = window.attributes
            params.preferredDisplayModeId = mode.modeId
            window.attributes = params
        }
    }

    /**
     * Force the display refresh rate globally via System/Secure Settings (where supported).
     * Requires WRITE_SECURE_SETTINGS or WRITE_SETTINGS.
     */
    suspend fun forceGlobalRefreshRate(context: Context, targetFps: Float): Boolean = withContext(Dispatchers.IO) {
        try {
            // Android 11+ global refresh rate keys
            Settings.System.putFloat(context.contentResolver, "min_refresh_rate", targetFps)
            Settings.System.putFloat(context.contentResolver, "peak_refresh_rate", targetFps)
            true
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Prioritize foreground threads for active gaming execution.
     */
    suspend fun prioritizeForegroundThreads() = withContext(Dispatchers.IO) {
        try {
            // Give rendering and main threads the highest scheduling priority
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY)
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
