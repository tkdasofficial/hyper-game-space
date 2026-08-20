package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.components.StatItem
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                HyperGameSpaceApp(viewModel)
            }
        }
    }
}

@Composable
fun HyperGameSpaceApp(viewModel: MainViewModel = viewModel()) {
    val games by viewModel.installedGames.collectAsStateWithLifecycle()
    val metrics by viewModel.metricsState.collectAsStateWithLifecycle()
    
    var selectedGameIndex by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF050505) // Premium deep black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Game List (Dialer Picker)
                val itemHeight = 88.dp
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
                val coroutineScope = rememberCoroutineScope()
                
                LaunchedEffect(listState, games.size) {
                    snapshotFlow { 
                        val layoutInfo = listState.layoutInfo
                        val center = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2
                        var closestIndex = -1
                        var minDistance = Int.MAX_VALUE
                        for (item in layoutInfo.visibleItemsInfo) {
                            val itemCenter = item.offset + item.size / 2
                            val distance = kotlin.math.abs(itemCenter - center)
                            if (distance < minDistance) {
                                minDistance = distance
                                closestIndex = item.index
                            }
                        }
                        closestIndex
                    }.collect { index ->
                        if (index in games.indices) {
                            selectedGameIndex = index
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .width(300.dp)
                        .height(itemHeight * 3)
                        .padding(start = 32.dp, end = 16.dp),
                    contentPadding = PaddingValues(top = itemHeight, bottom = itemHeight),
                    flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = listState)
                ) {
                    itemsIndexed(games) { index, game ->
                        val isSelected = index == selectedGameIndex
                        val scale by animateFloatAsState(targetValue = if (isSelected) 1f else 0.85f, label = "scale")
                        val alpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.5f, label = "alpha")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF1A1A1A) else Color.Transparent)
                                .clickable {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                    if (!game.isSystemBox) {
                                        viewModel.toggleGameSelection(game.packageName)
                                    }
                                }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Game Icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(if (isSelected) 8.dp else 0.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (game.isSystemBox) SolidColor(Color(0xFF2A2A2A))
                                        else Brush.linearGradient(listOf(Color(0xFFE53935), Color(0xFFE53935).copy(alpha = 0.7f)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (game.isSystemBox) {
                                    Icon(Icons.Filled.Widgets, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                game.name,
                                color = if (isSelected) Color.White else Color(0xFFAAAAAA),
                                fontSize = 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Center Area: Hero Image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 56.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF0F0F0F)) // Very dark grey
                ) {
                    val selectedGame = games.getOrNull(selectedGameIndex)
                    
                    // Subtle red glow behind the icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF330A0A), // Dark red center
                                        Color(0xFF0F0F0F)  // Fade to dark grey
                                    ),
                                    radius = 600f
                                )
                            )
                    )

                    // Static Game Icon
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                rotationZ = 12f
                                shadowElevation = 24.dp.toPx()
                                shape = RoundedCornerShape(24.dp)
                                clip = true
                            }
                            .background(
                                if (selectedGame?.isSystemBox == true) SolidColor(Color(0xFF2A2A2A))
                                else Brush.linearGradient(listOf(Color(0xFFE53935), Color(0xFFE53935).copy(alpha = 0.5f)))
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedGame?.isSystemBox == true) {
                            Icon(Icons.Filled.Widgets, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                        } else {
                            Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Right Area: Play Button
                Box(
                    modifier = Modifier
                        .padding(end = 32.dp)
                        .width(160.dp)
                        .height(112.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFD32F2F), Color(0xFFFF5252)) // Premium Red gradient
                            )
                        )
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Play", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(start = 32.dp, end = 32.dp, top = 10.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side (Real-Time FPS, RAM, Ping)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatItem(text = "${metrics.fps}", label = "FPS")
                    StatItem(text = "${metrics.ramPercentage}%", label = "RAM")
                    StatItem(text = metrics.ping, label = "PING")
                    StatItem(text = metrics.gpuLoad, label = "GPU")
                }

                // Right Side (Add & Settings)
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 360)
@Composable
fun PreviewHyperGameSpace() {
    MyApplicationTheme {
        HyperGameSpaceApp()
    }
}
