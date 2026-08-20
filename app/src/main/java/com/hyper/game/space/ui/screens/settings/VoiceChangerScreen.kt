package com.hyper.game.space.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChangerScreen(viewModel: VoiceChangerViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEnabled = state.isVoiceChangerEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Changer", color = Color.White) },
                actions = {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { viewModel.toggleVoiceChanger(it) },
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
            Text("Voice Profiles", color = if (isEnabled) Color.White else Color.Gray, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.profiles) { profile ->
                    Card(
                        modifier = Modifier
                            .width(120.dp)
                            .height(80.dp)
                            .clickable(enabled = isEnabled) { viewModel.setProfile(profile) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.selectedProfile == profile && isEnabled) MaterialTheme.colorScheme.primary else Color(0xFF1E293B)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(profile, color = if (isEnabled) Color.White else Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Pitch Shift", color = if (isEnabled) Color.White else Color.Gray)
            Slider(value = state.pitch, onValueChange = { viewModel.updatePitch(it) }, enabled = isEnabled)
            
            Spacer(modifier = Modifier.height(16.dp))

            Text("Timbre / Resonance", color = if (isEnabled) Color.White else Color.Gray)
            Slider(value = state.timbre, onValueChange = { viewModel.updateTimbre(it) }, enabled = isEnabled)
        }
    }
}
