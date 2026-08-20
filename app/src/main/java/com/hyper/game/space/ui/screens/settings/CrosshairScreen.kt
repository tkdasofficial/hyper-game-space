package com.hyper.game.space.ui.screens.settings

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
fun CrosshairScreen(viewModel: CrosshairViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    val isEnabled = state.isCrosshairEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crosshair Customizer", color = Color.White) },
                actions = {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { viewModel.toggleCrosshair(it) },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0x44000000))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Crosshair Style", color = if (isEnabled) Color.White else Color.Gray, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded && isEnabled,
                onExpandedChange = { if (isEnabled) expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = state.style,
                    onValueChange = {},
                    readOnly = true,
                    enabled = isEnabled,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded && isEnabled,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.styles.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.setStyle(option)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Scale", color = if (isEnabled) Color.White else Color.Gray)
            Slider(
                value = state.scale,
                onValueChange = { viewModel.updateScale(it) },
                valueRange = 0.5f..2.0f,
                enabled = isEnabled
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ADS Auto-Hide", color = if (isEnabled) Color.White else Color.Gray, style = MaterialTheme.typography.titleMedium)
                    Text("Hide when aiming down sights", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = state.adsAutoHide,
                    onCheckedChange = { viewModel.toggleAdsAutoHide(it) },
                    enabled = isEnabled
                )
            }
        }
    }
}
