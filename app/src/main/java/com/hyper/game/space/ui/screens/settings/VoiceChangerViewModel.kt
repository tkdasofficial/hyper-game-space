package com.hyper.game.space.ui.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VoiceChangerState(
    val isVoiceChangerEnabled: Boolean = false,
    val selectedProfile: String = "Normal",
    val pitch: Float = 0.5f,
    val timbre: Float = 0.5f
)

class VoiceChangerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VoiceChangerState())
    val uiState = _uiState.asStateFlow()
    
    val profiles = listOf("Normal", "Robot", "Chipmunk", "Deep Voice")

    fun toggleVoiceChanger(enabled: Boolean) = _uiState.update { it.copy(isVoiceChangerEnabled = enabled) }
    fun setProfile(profile: String) = _uiState.update { it.copy(selectedProfile = profile) }
    fun updatePitch(value: Float) = _uiState.update { it.copy(pitch = value) }
    fun updateTimbre(value: Float) = _uiState.update { it.copy(timbre = value) }
}
