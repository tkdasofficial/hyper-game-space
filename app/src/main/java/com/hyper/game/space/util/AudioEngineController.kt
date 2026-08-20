package com.hyper.game.space.util

import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AudioEngineController {
    private const val TAG = "AudioEngineController"
    private var equalizer: Equalizer? = null
    private var dynamicsProcessing: DynamicsProcessing? = null

    suspend fun enableFootstepEQ(priority: Int = 0) = withContext(Dispatchers.IO) {
        disableAudioEffects()

        try {
            // Priority 0, AudioSessionId 0 applies to Global output on many devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Try modern DynamicsProcessing first for better quality
                val builder = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    2, true, 8, true, 8, true, 8, true
                )
                
                dynamicsProcessing = DynamicsProcessing(priority, 0, builder.build())
                dynamicsProcessing?.enabled = true
                Log.d(TAG, "Initialized DynamicsProcessing for EQ")
            } else {
                equalizer = Equalizer(priority, 0)
                equalizer?.enabled = true
                
                val numBands = equalizer?.numberOfBands ?: 0
                val minEq = equalizer?.bandLevelRange?.get(0) ?: -1500
                val maxEq = equalizer?.bandLevelRange?.get(1) ?: 1500

                for (i in 0 until numBands) {
                    val freqHz = (equalizer?.getCenterFreq(i.toShort()) ?: 0) / 1000
                    when {
                        // Boost footstep ranges (1kHz - 4kHz)
                        freqHz in 1000..4000 -> equalizer?.setBandLevel(i.toShort(), (maxEq * 0.8f).toInt().toShort())
                        // Suppress muddy bass (<250Hz)
                        freqHz < 250 -> equalizer?.setBandLevel(i.toShort(), (minEq * 0.4f).toInt().toShort())
                        else -> equalizer?.setBandLevel(i.toShort(), 0)
                    }
                }
                Log.d(TAG, "Initialized Legacy Equalizer for EQ")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind to Audio Session 0. System may restrict global EQ without root.", e)
            disableAudioEffects()
        }
    }

    suspend fun disableAudioEffects() = withContext(Dispatchers.IO) {
        try {
            equalizer?.enabled = false
            equalizer?.release()
            dynamicsProcessing?.enabled = false
            dynamicsProcessing?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio FX", e)
        } finally {
            equalizer = null
            dynamicsProcessing = null
        }
    }
}
