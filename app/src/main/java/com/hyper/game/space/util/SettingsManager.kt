package com.hyper.game.space.util

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "hyper_game_settings"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Virtual Sensitivity
    fun getVirtualSensitivityX(context: Context): Float = getPrefs(context).getFloat("virt_sens_x", 1.0f)
    fun setVirtualSensitivityX(context: Context, value: Float) = getPrefs(context).edit().putFloat("virt_sens_x", value).apply()

    fun getVirtualSensitivityY(context: Context): Float = getPrefs(context).getFloat("virt_sens_y", 1.0f)
    fun setVirtualSensitivityY(context: Context, value: Float) = getPrefs(context).edit().putFloat("virt_sens_y", value).apply()

    // Graphics
    fun getForceFps(context: Context): Boolean = getPrefs(context).getBoolean("force_fps", false)
    fun setForceFps(context: Context, value: Boolean) = getPrefs(context).edit().putBoolean("force_fps", value).apply()

    fun getDisableVSync(context: Context): Boolean = getPrefs(context).getBoolean("disable_vsync", true)
    fun setDisableVSync(context: Context, value: Boolean) = getPrefs(context).edit().putBoolean("disable_vsync", value).apply()

    fun getGameDriver(context: Context): Boolean = getPrefs(context).getBoolean("game_driver", false)
    fun setGameDriver(context: Context, value: Boolean) = getPrefs(context).edit().putBoolean("game_driver", value).apply()

    // Color Filter
    fun getColorProfile(context: Context): String = getPrefs(context).getString("color_profile", "NONE") ?: "NONE"
    fun setColorProfile(context: Context, value: String) = getPrefs(context).edit().putString("color_profile", value).apply()

    // Audio
    fun getFootstepEQ(context: Context): Boolean = getPrefs(context).getBoolean("footstep_eq", false)
    fun setFootstepEQ(context: Context, value: Boolean) = getPrefs(context).edit().putBoolean("footstep_eq", value).apply()
}
