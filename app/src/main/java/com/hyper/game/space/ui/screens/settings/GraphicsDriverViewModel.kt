package com.hyper.game.space.ui.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GraphicsDriverState(
    val driverMode: String = "System Default",
    val vsyncEnabled: Boolean = false,
    val forceFpsLock: Boolean = false
)

class GraphicsDriverViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GraphicsDriverState())
    val uiState = _uiState.asStateFlow()
    
    val driverOptions = listOf("System Default", "Game Driver", "Prerelease Driver")

    fun setDriverMode(mode: String) = _uiState.update { it.copy(driverMode = mode) }
    fun toggleVsync(enabled: Boolean) = _uiState.update { it.copy(vsyncEnabled = enabled) }
    fun toggleFpsLock(enabled: Boolean) = _uiState.update { it.copy(forceFpsLock = enabled) }
}
