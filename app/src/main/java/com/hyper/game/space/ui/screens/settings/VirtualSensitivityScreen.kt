package com.hyper.game.space.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualSensitivityScreen(viewModel: VirtualSensitivityViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Virtual Sensitivity", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0x44000000))
            )
        },
        containerColor = Color(0xFF0F172A) // Dark glassmorphic base
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            // Axis Sensitivity Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Axis Sensitivity (X, Y, Z)", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = state.isAxisSensitivityEnabled,
                    onCheckedChange = { viewModel.toggleAxisSensitivity(it) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = state.xAxisSens,
                onValueChange = { viewModel.updateXAxis(it) },
                valueRange = 0.5f..3.0f,
                enabled = state.isAxisSensitivityEnabled
            )
            Text("X-Axis: ${"%.2f".format(state.xAxisSens)}x", color = if (state.isAxisSensitivityEnabled) Color.White else Color.Gray)
            
            Slider(
                value = state.yAxisSens,
                onValueChange = { viewModel.updateYAxis(it) },
                valueRange = 0.5f..3.0f,
                enabled = state.isAxisSensitivityEnabled
            )
            Text("Y-Axis: ${"%.2f".format(state.yAxisSens)}x", color = if (state.isAxisSensitivityEnabled) Color.White else Color.Gray)
            
            Slider(
                value = state.zAxisSens,
                onValueChange = { viewModel.updateZAxis(it) },
                valueRange = 0.5f..3.0f,
                enabled = state.isAxisSensitivityEnabled
            )
            Text("Z-Axis (Gyro): ${"%.2f".format(state.zAxisSens)}x", color = if (state.isAxisSensitivityEnabled) Color.White else Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // Touch Response Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Touch Response Booster", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = state.isTouchResponseEnabled,
                    onCheckedChange = { viewModel.toggleTouchResponse(it) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = state.touchResponseRate,
                onValueChange = { viewModel.updateTouchResponseRate(it) },
                valueRange = 60f..240f,
                steps = 3,
                enabled = state.isTouchResponseEnabled
            )
            Text("Sampling Rate: ${state.touchResponseRate.toInt()} Hz", color = if (state.isTouchResponseEnabled) Color.White else Color.Gray)
        }
    }
}
