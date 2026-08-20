package com.hyper.game.space.ui.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VirtualSensitivityState(
    val isAxisSensitivityEnabled: Boolean = false,
    val isTouchResponseEnabled: Boolean = false,
    val xAxisSens: Float = 1.0f,
    val yAxisSens: Float = 1.0f,
    val zAxisSens: Float = 1.0f,
    val touchResponseRate: Float = 120f
)

class VirtualSensitivityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VirtualSensitivityState())
    val uiState: StateFlow<VirtualSensitivityState> = _uiState.asStateFlow()

    fun toggleAxisSensitivity(enabled: Boolean) = _uiState.update { it.copy(isAxisSensitivityEnabled = enabled) }
    fun toggleTouchResponse(enabled: Boolean) = _uiState.update { it.copy(isTouchResponseEnabled = enabled) }
    fun updateXAxis(value: Float) = _uiState.update { it.copy(xAxisSens = value) }
    fun updateYAxis(value: Float) = _uiState.update { it.copy(yAxisSens = value) }
    fun updateZAxis(value: Float) = _uiState.update { it.copy(zAxisSens = value) }
    fun updateTouchResponseRate(value: Float) = _uiState.update { it.copy(touchResponseRate = value) }
}
