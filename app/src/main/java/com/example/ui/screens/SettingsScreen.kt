package com.example.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import com.example.util.SettingsManager

val categories = listOf(
    "Performance Mode",
    "Virtual Sensitivity",
    "Graphics Driver & Display",
    "System Optimizer",
    "Crosshair Customizer",
    "Game Audio Equalizer",
    "Game DND Mode",
    "Network & Connection",
    "Screen Recorder",
    "Voice Changer"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF050505),
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F0F)
                )
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Left: Categories
            LazyColumn(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0A0A0A)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFD32F2F).copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedCategory = category }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color(0xFFFF5252) else Color(0xFFAAAAAA),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Right: Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF050505))
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                when (selectedCategory) {
                    "Performance Mode" -> PerformanceModeSettings()
                    "Virtual Sensitivity" -> VirtualSensitivitySettings()
                    "Graphics Driver & Display" -> GraphicsSettings()
                    "System Optimizer" -> SystemOptimizerSettings()
                    "Crosshair Customizer" -> CrosshairSettings()
                    "Game Audio Equalizer" -> AudioSettings()
                    "Game DND Mode" -> DndSettings()
                    "Network & Connection" -> NetworkSettings()
                    "Screen Recorder" -> ScreenRecorderSettings()
                    "Voice Changer" -> VoiceChangerSettings()
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            text = title,
            color = Color(0xFFAAAAAA),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun PerformanceModeSettings() {
    Column(modifier = Modifier.fillMaxSize()) {
        
        var selectedMode by remember { mutableStateOf("BALANCED") }
        val modes = listOf(
            "COOLING" to "Thermal & battery protection focus",
            "BALANCED" to "Default system efficiency",
            "ULTRA" to "High FPS & CPU priority execution",
            "EXTREME" to "Uncapped peak CPU/GPU output"
        )
        
        modes.forEach { (mode, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedMode == mode) Color(0xFF1A1A1A) else Color(0xFF0A0A0A))
                    .clickable { selectedMode = mode }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMode == mode,
                    onClick = { selectedMode = mode },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF5252), unselectedColor = Color(0xFF555555))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(mode, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(desc, color = Color(0xFFAAAAAA), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SliderSetting(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit, formatValue: (Float) -> String = { it.toInt().toString() }) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 16.sp)
            Text(formatValue(value), color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF5252),
                activeTrackColor = Color(0xFFFF5252),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}

@Composable
fun SwitchSetting(label: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A0A0A))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(desc, color = Color(0xFFAAAAAA), fontSize = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF5252),
                uncheckedThumbColor = Color(0xFFAAAAAA),
                uncheckedTrackColor = Color(0xFF333333)
            )
        )
    }
}

@Composable
fun VirtualSensitivitySettings(context: Context = androidx.compose.ui.platform.LocalContext.current) {
    Column(modifier = Modifier.fillMaxSize()) {
        
        var xAxis by remember { mutableFloatStateOf(SettingsManager.getVirtualSensitivityX(context) * 100f) }
        var yAxis by remember { mutableFloatStateOf(SettingsManager.getVirtualSensitivityY(context) * 100f) }
        var zAxis by remember { mutableFloatStateOf(100f) }
        var trr by remember { mutableFloatStateOf(100f) }
        
        SettingsSection("Axes Sensitivity") {
            SliderSetting("X-Axis Sensitivity", xAxis, 0f..1000f, { 
                xAxis = it
                SettingsManager.setVirtualSensitivityX(context, it / 100f)
            })
            SliderSetting("Y-Axis Sensitivity", yAxis, 0f..1000f, { 
                yAxis = it
                SettingsManager.setVirtualSensitivityY(context, it / 100f)
            })
            SliderSetting("Z-Axis / Gyro Sensitivity", zAxis, 0f..1000f, { zAxis = it })
        }
        
        SettingsSection("Response Rate") {
            SliderSetting("Touch Response Rate / TRR", trr, 0f..1000f, { trr = it })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSetting(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(label, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF5252),
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1A1A1A))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.White) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GraphicsSettings(context: Context = androidx.compose.ui.platform.LocalContext.current) {
    Column(modifier = Modifier.fillMaxSize()) {
        
        val initialDriver = if (SettingsManager.getGameDriver(context)) "Game Driver" else "Default"
        var driverMode by remember { mutableStateOf(initialDriver) }
        var frameRate by remember { mutableStateOf("60") }
        var resolution by remember { mutableStateOf("100%") }
        var vsync by remember { mutableStateOf(!SettingsManager.getDisableVSync(context)) }
        var forceFps by remember { mutableStateOf(SettingsManager.getForceFps(context)) }
        
        SettingsSection("Driver") {
            DropdownSetting("Driver Mode", listOf("Default", "Game Driver", "System Graphics"), driverMode) { 
                driverMode = it
                SettingsManager.setGameDriver(context, it == "Game Driver")
            }
            
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset GPU Shader Cache")
            }
        }
        
        SettingsSection("Display") {
            DropdownSetting("Frame Rate Output", listOf("30", "60", "90", "120", "EXTREME"), frameRate) { frameRate = it }
            DropdownSetting("Display Resolution Scaler", listOf("50%", "75%", "100%"), resolution) { resolution = it }
            SwitchSetting("V-Sync & Frame Pacing", "Prevent screen tearing", vsync) { vsync = it }
            SwitchSetting("Force FPS Lock", "Maintain stable frame rate (max -5 FPS drop)", forceFps) { forceFps = it }
        }
    }
}

@Composable
fun SystemOptimizerSettings() {
    Column(modifier = Modifier.fillMaxSize()) {
        
        var throttle by remember { mutableStateOf(true) }
        var touchFilter by remember { mutableStateOf(false) }
        var touchCalib by remember { mutableStateOf(true) }
        
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Text("Memory Hard-Flush (Clear Stutter/Leaks)")
        }
        
        SwitchSetting("Throttling Stabilizer", "Sustained clock speed locking", throttle) { throttle = it }
        SwitchSetting("Auto Touch Filter", "Moisture & heat input correction", touchFilter) { touchFilter = it }
        SwitchSetting("Touch Sampling Calibration", "Latency removal", touchCalib) { touchCalib = it }
    }
}

@Composable
fun CrosshairSettings(context: Context = androidx.compose.ui.platform.LocalContext.current) {
    Column(modifier = Modifier.fillMaxSize()) {
        
        var style by remember { mutableStateOf("Crosshair") }
        var adsHide by remember { mutableStateOf(true) }
        var scale by remember { mutableFloatStateOf(20f) }
        var selectedFilter by remember { mutableStateOf(SettingsManager.getColorProfile(context)) }
        
        DropdownSetting("Style Engine", listOf("Dot", "Crosshair", "Circle", "T-Shape", "Custom SVG"), style) { style = it }
        
        SliderSetting("Scale", scale, 1f..50f, { scale = it })
        
        // Color Matrix
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A0A0A))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Vision Color Matrix", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(if (selectedFilter == "NONE") Color.White else Color.Gray).clickable { 
                    selectedFilter = "NONE"
                    SettingsManager.setColorProfile(context, "NONE") 
                })
                Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(Color.Red).clickable { 
                    selectedFilter = "HIGH_CONTRAST"
                    SettingsManager.setColorProfile(context, "HIGH_CONTRAST") 
                })
                Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(Color.Green).clickable { 
                    selectedFilter = "GRASS_SPOTTER"
                    SettingsManager.setColorProfile(context, "GRASS_SPOTTER") 
                })
                Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(Color.DarkGray).clickable { 
                    selectedFilter = "SHADOW_BOOST"
                    SettingsManager.setColorProfile(context, "SHADOW_BOOST") 
                })
            }
        }
        
        SwitchSetting("ADS Auto-Hide", "Dynamically hides during scope view", adsHide) { adsHide = it }
    }
}

@Composable
fun AudioSettings(context: Context = androidx.compose.ui.platform.LocalContext.current) {
    Column(modifier = Modifier.fillMaxSize()) {
        
        var preset by remember { mutableStateOf(if (SettingsManager.getFootstepEQ(context)) "FPS Tactical" else "Default") }
        var footstep by remember { mutableFloatStateOf(80f) }
        var gunshot by remember { mutableFloatStateOf(40f) }
        
        DropdownSetting("EQ Presets", listOf("Default", "FPS Tactical", "Bass Boost", "Clear Voice", "Custom"), preset) { 
            preset = it
            SettingsManager.setFootstepEQ(context, it == "FPS Tactical")
        }
        
        SliderSetting("Footstep Booster (High-Freq)", footstep, 0f..100f, { footstep = it })
        SliderSetting("Gunshot Filter (Bass Dampening)", gunshot, 0f..100f, { gunshot = it })
    }
}

@Composable
fun DndSettings() {
    Column(modifier = Modifier.fillMaxSize()) {
        
        var calls by remember { mutableStateOf(true) }
        var autoReject by remember { mutableStateOf(false) }
        var banners by remember { mutableStateOf(true) }
        var brightness by remember { mutableStateOf(true) }
        
        SwitchSetting("Block Call Banners", "Background silent call handling", calls) { calls = it }
        SwitchSetting("Auto Reject Calls", "Instant call decline", autoReject) { autoReject = it }
        SwitchSetting("Notification Banner Suppress", "Zero pop-up interruption", banners) { banners = it }
        SwitchSetting("Brightness Lock", "Fixed panel luminance", brightness) { brightness = it }
    }
}

@Composable
fun NetworkSettings() {
    Column(modifier = Modifier.fillMaxSize()) {
        
        var optimize by remember { mutableStateOf(true) }
        var restrict by remember { mutableStateOf(true) }
        var failover by remember { mutableStateOf(false) }
        
        SwitchSetting("Network Optimizer", "Gaming packet prioritization", optimize) { optimize = it }
        SwitchSetting("Background Data Restrictor", "Bandwidth locking", restrict) { restrict = it }
        SwitchSetting("Auto-Switch Engine", "Seamless Wi-Fi/Mobile data failover", failover) { failover = it }
    }
}

@Composable
fun ScreenRecorderSettings() {
    Column(modifier = Modifier.fillMaxSize()) {
        
        var res by remember { mutableStateOf("1080p") }
        var fps by remember { mutableStateOf("60 FPS") }
        var audio by remember { mutableStateOf("Dual Audio") }
        var bitrate by remember { mutableFloatStateOf(20f) }
        var touch by remember { mutableStateOf(false) }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                DropdownSetting("Resolution", listOf("720p", "1080p", "2K", "Native"), res) { res = it }
            }
            Box(modifier = Modifier.weight(1f)) {
                DropdownSetting("Frame Rate", listOf("30 FPS", "60 FPS", "90 FPS", "120 FPS"), fps) { fps = it }
            }
        }
        
        DropdownSetting("Audio Source", listOf("System", "Mic", "Dual Audio"), audio) { audio = it }
        
        SliderSetting("Bitrate Control (Mbps)", bitrate, 8f..50f, { bitrate = it })
        
        SwitchSetting("Touch Indicators", "Visual tap tracking", touch) { touch = it }
    }
}

@Composable
fun VoiceChangerSettings() {
    Column(modifier = Modifier.fillMaxSize()) {
        
        var profile by remember { mutableStateOf("Original") }
        var pitch by remember { mutableFloatStateOf(0f) }
        var monitor by remember { mutableStateOf(false) }
        
        DropdownSetting("Voice Profiles", listOf("Original", "Robot", "Girl", "Monster", "Cartoon", "Cyberpunk"), profile) { profile = it }
        
        SliderSetting("Pitch Adjustment", pitch, -5f..5f, { pitch = it }, formatValue = { String.format("%.1f", it) })
        
        SwitchSetting("Mic Monitor & Preview", "Live test buffer", monitor) { monitor = it }
    }
}
