package com.hyper.game.space.ui.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScreenRecorderState(
    val isScreenRecorderEnabled: Boolean = false,
    val resolution: String = "1080p",
    val frameRate: String = "60FPS",
    val audioSource: String = "Internal Audio",
    val bitrate: Float = 16f
)

class ScreenRecorderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScreenRecorderState())
    val uiState = _uiState.asStateFlow()
    
    val resolutions = listOf("720p", "1080p", "1440p")
    val frameRates = listOf("30FPS", "60FPS", "90FPS", "120FPS")
    val audioSources = listOf("Internal Audio", "Microphone", "Both", "Muted")

    fun toggleRecorder(enabled: Boolean) = _uiState.update { it.copy(isScreenRecorderEnabled = enabled) }
    fun setResolution(res: String) = _uiState.update { it.copy(resolution = res) }
    fun setFrameRate(fps: String) = _uiState.update { it.copy(frameRate = fps) }
    fun setAudioSource(src: String) = _uiState.update { it.copy(audioSource = src) }
    fun updateBitrate(bitrate: Float) = _uiState.update { it.copy(bitrate = bitrate) }
}
