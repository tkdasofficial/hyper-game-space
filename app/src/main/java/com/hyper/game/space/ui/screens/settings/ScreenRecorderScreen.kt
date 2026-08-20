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
fun ScreenRecorderScreen(viewModel: ScreenRecorderViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var resExpanded by remember { mutableStateOf(false) }
    var fpsExpanded by remember { mutableStateOf(false) }
    var audioExpanded by remember { mutableStateOf(false) }
    val isEnabled = state.isScreenRecorderEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen Recorder", color = Color.White) },
                actions = {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { viewModel.toggleRecorder(it) },
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
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = resExpanded && isEnabled,
                    onExpandedChange = { if (isEnabled) resExpanded = !resExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = state.resolution,
                        onValueChange = {},
                        label = { Text("Resolution") },
                        readOnly = true,
                        enabled = isEnabled,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = resExpanded && isEnabled,
                        onDismissRequest = { resExpanded = false }
                    ) {
                        viewModel.resolutions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { viewModel.setResolution(option); resExpanded = false })
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = fpsExpanded && isEnabled,
                    onExpandedChange = { if (isEnabled) fpsExpanded = !fpsExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = state.frameRate,
                        onValueChange = {},
                        label = { Text("Frame Rate") },
                        readOnly = true,
                        enabled = isEnabled,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fpsExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fpsExpanded && isEnabled,
                        onDismissRequest = { fpsExpanded = false }
                    ) {
                        viewModel.frameRates.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { viewModel.setFrameRate(option); fpsExpanded = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            ExposedDropdownMenuBox(
                expanded = audioExpanded && isEnabled,
                onExpandedChange = { if (isEnabled) audioExpanded = !audioExpanded }
            ) {
                OutlinedTextField(
                    value = state.audioSource,
                    onValueChange = {},
                    label = { Text("Audio Source") },
                    readOnly = true,
                    enabled = isEnabled,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = audioExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = audioExpanded && isEnabled,
                    onDismissRequest = { audioExpanded = false }
                ) {
                    viewModel.audioSources.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { viewModel.setAudioSource(option); audioExpanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Bitrate: ${state.bitrate.toInt()} Mbps", color = if (isEnabled) Color.White else Color.Gray)
            Slider(
                value = state.bitrate,
                onValueChange = { viewModel.updateBitrate(it) },
                valueRange = 4f..50f,
                enabled = isEnabled
            )
        }
    }
}
