package com.example.util

import android.media.audiofx.Equalizer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FootstepAudioBooster {
    private var equalizer: Equalizer? = null
    private const val TAG = "FootstepAudioBooster"

    /**
     * Intercepts the global AudioSessionId (0) using Android's native Equalizer API.
     * Modulates frequency bands targeting common game audio bounds (1kHz-4kHz) for footsteps.
     */
    suspend fun enableEnhancement(priority: Int = 0) = withContext(Dispatchers.IO) {
        if (equalizer != null) return@withContext

        try {
            // Note: Session 0 is deprecated for general media apps, but remains 
            // the official path to apply Global EQ without root / system permissions.
            equalizer = Equalizer(priority, 0)
            equalizer?.enabled = true

            val numBands = equalizer?.numberOfBands ?: 0
            val minEqLevel = equalizer?.bandLevelRange?.get(0) ?: -1500
            val maxEqLevel = equalizer?.bandLevelRange?.get(1) ?: 1500

            for (i in 0 until numBands) {
                val centerFreq = equalizer?.getCenterFreq(i.toShort()) ?: 0
                // Frequencies returned are in milliHertz (mHz), convert to Hz.
                val freqHz = centerFreq / 1000

                when {
                    // Footsteps, gravel crunches, shell casings, and reloads (usually between 1000Hz - 4000Hz)
                    freqHz in 1000..4000 -> {
                        // Boost to 80% of maximum permitted gain
                        val targetLevel = (maxEqLevel * 0.8f).toInt().toShort()
                        equalizer?.setBandLevel(i.toShort(), targetLevel)
                    }
                    
                    // Heavy artillery, distant explosions, wind rumble (muddying frequencies < 250Hz)
                    freqHz < 250 -> {
                        // Suppress bass directly to clear mid-high channels
                        val targetLevel = (minEqLevel * 0.4f).toInt().toShort()
                        equalizer?.setBandLevel(i.toShort(), targetLevel)
                    }
                    
                    // Maintain standard flat response for dialogue and music (~500Hz, >5000Hz)
                    else -> {
                        equalizer?.setBandLevel(i.toShort(), 0)
                    }
                }
            }
            Log.d(TAG, "Footstep audio enhancement initialized and locked.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Global Equalizer. Device OEM may restrict Session 0.", e)
            disableEnhancement()
        }
    }

    /**
     * Gracefully disables and releases the hardware AudioFX buffers to prevent system memory leaks.
     */
    suspend fun disableEnhancement() = withContext(Dispatchers.IO) {
        try {
            equalizer?.enabled = false
            equalizer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio resources", e)
        } finally {
            equalizer = null
            Log.d(TAG, "Footstep audio enhancement released.")
        }
    }
}
