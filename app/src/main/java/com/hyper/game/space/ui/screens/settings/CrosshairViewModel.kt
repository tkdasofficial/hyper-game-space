package com.hyper.game.space.ui.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CrosshairState(
    val isCrosshairEnabled: Boolean = false,
    val style: String = "Dot",
    val scale: Float = 1.0f,
    val colorIndex: Int = 0,
    val adsAutoHide: Boolean = true
)

class CrosshairViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CrosshairState())
    val uiState = _uiState.asStateFlow()
    
    val styles = listOf("Dot", "Cross", "Circle", "Reticle")

    fun toggleCrosshair(enabled: Boolean) = _uiState.update { it.copy(isCrosshairEnabled = enabled) }
    fun setStyle(style: String) = _uiState.update { it.copy(style = style) }
    fun updateScale(scale: Float) = _uiState.update { it.copy(scale = scale) }
    fun setColorIndex(index: Int) = _uiState.update { it.copy(colorIndex = index) }
    fun toggleAdsAutoHide(enabled: Boolean) = _uiState.update { it.copy(adsAutoHide = enabled) }
}
