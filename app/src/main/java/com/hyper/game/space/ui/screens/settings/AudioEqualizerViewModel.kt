package com.hyper.game.space.ui.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AudioEqualizerState(
    val isAudioEqualizerEnabled: Boolean = false,
    val preset: String = "Default",
    val lowFreq: Float = 0.5f,
    val midFreq: Float = 0.5f,
    val highFreq: Float = 0.5f
)

class AudioEqualizerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AudioEqualizerState())
    val uiState = _uiState.asStateFlow()
    
    val presets = listOf("Default", "Footstep Booster", "Gunshot Filter", "Bass Boost")

    fun toggleEqualizer(enabled: Boolean) = _uiState.update { it.copy(isAudioEqualizerEnabled = enabled) }
    fun setPreset(preset: String) = _uiState.update { it.copy(preset = preset) }
    fun updateLowFreq(value: Float) = _uiState.update { it.copy(lowFreq = value) }
    fun updateMidFreq(value: Float) = _uiState.update { it.copy(midFreq = value) }
    fun updateHighFreq(value: Float) = _uiState.update { it.copy(highFreq = value) }
}
