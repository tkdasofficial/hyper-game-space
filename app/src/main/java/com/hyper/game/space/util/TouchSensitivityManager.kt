package com.hyper.game.space.util

import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TouchSensitivityManager {

    /**
     * Adjusts the standard Android pointer speed (Touch Responsiveness / TRR).
     * Usually ranges from 1 to 7 (4 is the default).
     */
    suspend fun setPointerSpeed(context: Context, speed: Int): Boolean = withContext(Dispatchers.IO) {
        try {
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
     * Retrieve the current system pointer speed.
     */
    suspend fun getPointerSpeed(context: Context): Int = withContext(Dispatchers.IO) {
        try {
            Settings.System.getInt(context.contentResolver, "pointer_speed", 4)
        } catch (e: Settings.SettingNotFoundException) {
            4
        }
    }
    
    /**
     * Experimental: Enables high touch sensitivity mode (usually Glove Mode or Screen Protector mode)
     * often mapped to these specific keys on custom ROMs (Samsung, MIUI).
     */
    suspend fun setOemHighTouchSensitivity(context: Context, enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            Settings.System.putInt(
                context.contentResolver, 
                "touch_sensitivity_enabled", 
                if (enabled) 1 else 0
            )
            true
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
