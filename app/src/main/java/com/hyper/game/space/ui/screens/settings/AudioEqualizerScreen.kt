package com.hyper.game.space.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEqualizerScreen(viewModel: AudioEqualizerViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    val isEnabled = state.isAudioEqualizerEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Audio Equalizer", color = Color.White) },
                actions = {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { viewModel.toggleEqualizer(it) },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0x44000000))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)
        ) {
            Text("Audio Preset", color = if (isEnabled) Color.White else Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded && isEnabled,
                onExpandedChange = { if (isEnabled) expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = state.preset,
                    onValueChange = {},
                    readOnly = true,
                    enabled = isEnabled,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded && isEnabled,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.presets.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.setPreset(option)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Low Frequencies (Bass)", color = if (isEnabled) Color.White else Color.Gray)
            Slider(value = state.lowFreq, onValueChange = { viewModel.updateLowFreq(it) }, enabled = isEnabled)
            
            Text("Mid Frequencies", color = if (isEnabled) Color.White else Color.Gray)
            Slider(value = state.midFreq, onValueChange = { viewModel.updateMidFreq(it) }, enabled = isEnabled)
            
            Text("High Frequencies (Treble)", color = if (isEnabled) Color.White else Color.Gray)
            Slider(value = state.highFreq, onValueChange = { viewModel.updateHighFreq(it) }, enabled = isEnabled)
        }
    }
}
