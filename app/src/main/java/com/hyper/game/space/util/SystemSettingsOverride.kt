package com.hyper.game.space.util

import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SystemSettingsOverride {

    /**
     * Safely attempts to override Android's built-in pointer speed (Virtual DPI mapping).
     */
    suspend fun overridePointerSpeed(context: Context, speed: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            // Speed generally ranges from 1 to 7
            Settings.System.putInt(context.contentResolver, "pointer_speed", speed)
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
     * Attempts to force system graphic drivers (Game Driver vs Default).
     * Requires WRITE_SECURE_SETTINGS which is usually granted via adb or rooted devices.
     */
    suspend fun overrideGraphicsDriver(context: Context, enableGameDriver: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val value = if (enableGameDriver) "game" else "default"
            Settings.Global.putString(context.contentResolver, "gamedriver_opt_in_apps", if (enableGameDriver) context.packageName else "")
            true
        } catch (e: SecurityException) {
            // Expected on non-rooted devices without ADB setup
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
