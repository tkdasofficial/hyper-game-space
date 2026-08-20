package com.hyper.game.space.ui.screens.gamespace

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

val NeonRed = Color(0xFFFF0033)
val CyberWhite = Color(0xFFFFFFFF)
val MutedGrey = Color(0xFF2A2A2A)
val DarkGlass = Color(0xD9121212)

@Composable
fun DualEdgeSwipeBox(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isOverlayVisible by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val edgeWidthPx = remember(density) { with(density) { 40.dp.toPx() } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isEnabled, isLandscape) {
                if (!isEnabled || !isLandscape) return@pointerInput
                awaitEachGesture {
                    var leftDownTime = 0L
                    var rightDownTime = 0L
                    var swipeTriggered = false

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val changes = event.changes

                        if (changes.isEmpty()) break

                        changes.forEach { change ->
                            if (change.pressed && !change.previousPressed) {
                                if (change.position.x <= edgeWidthPx) {
                                    leftDownTime = change.uptimeMillis
                                } else if (change.position.x >= size.width - edgeWidthPx) {
                                    rightDownTime = change.uptimeMillis
                                }
                            }
                        }

                        if (!swipeTriggered && leftDownTime > 0 && rightDownTime > 0) {
                            val timeDiff = abs(leftDownTime - rightDownTime)
                            if (timeDiff <= 250L) {
                                var leftMovedIn = false
                                var rightMovedIn = false
                                changes.forEach { change ->
                                    if (change.position.x <= edgeWidthPx + 150f && change.position.x > change.previousPosition.x + 2f) {
                                        leftMovedIn = true
                                    }
                                    if (change.position.x >= size.width - edgeWidthPx - 150f && change.position.x < change.previousPosition.x - 2f) {
                                        rightMovedIn = true
                                    }
                                }
                                if (leftMovedIn && rightMovedIn) {
                                    isOverlayVisible = true
                                    swipeTriggered = true
                                    changes.forEach { it.consume() }
                                }
                            } else {
                                val current = changes.firstOrNull()?.uptimeMillis ?: 0L
                                if (leftDownTime > 0 && rightDownTime == 0L && current - leftDownTime > 250L) leftDownTime = 0L
                                if (rightDownTime > 0 && leftDownTime == 0L && current - rightDownTime > 250L) rightDownTime = 0L
                            }
                        }

                        if (changes.all { !it.pressed }) {
                            break
                        }
                    }
                }
            }
    ) {
        content()

        GameSpaceOverlayLayout(
            isVisible = isOverlayVisible,
            onClose = { isOverlayVisible = false }
        )
    }
}

@Composable
fun GameSpaceOverlayLayout(
    isVisible: Boolean,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }

    val leftOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else -screenWidth / 2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "leftWing"
    )
    val rightOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else screenWidth / 2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "rightWing"
    )
    val topOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else -300f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "topBar"
    )

    if (isVisible || leftOffset > -screenWidth / 2f + 10f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isVisible) Color.Black.copy(alpha = 0.5f) else Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        ) {
            TopStatusHeader(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, topOffset.roundToInt()) }
            )

            LeftGamingPanel(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.35f)
                    .offset { IntOffset(leftOffset.roundToInt(), 0) }
            )

            RightGamingPanel(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.35f)
                    .offset { IntOffset(rightOffset.roundToInt(), 0) }
            )
        }
    }
}

@Composable
fun LeftGamingPanel(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val shape = remember(density) {
        GenericShape { size, _ ->
            val offset = with(density) { 60.dp.toPx() }
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - offset, size.height)
            lineTo(0f, size.height)
            close()
        }
    }

    Box(
        modifier = modifier
            .drawWithCache {
                val offset = 60.dp.toPx()
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width - offset, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                onDrawBehind {
                    drawPath(path, DarkGlass)
                    drawLine(
                        color = NeonRed,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width - offset, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            .clip(shape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {} 
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickToolIcon(Icons.Default.Menu)
                QuickToolIcon(Icons.Default.Edit)
                QuickToolIcon(Icons.Default.Gamepad)
                QuickToolIcon(Icons.Default.Settings)
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 40.dp, end = 40.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "2.84", color = CyberWhite, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("GHz", color = NeonRed, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0x33FF0033), CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CPU", color = CyberWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                var isRiseMode by remember { mutableStateOf(true) }
                Button(
                    onClick = { isRiseMode = !isRiseMode },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRiseMode) NeonRed.copy(alpha = 0.2f) else MutedGrey.copy(alpha = 0.5f),
                        contentColor = if (isRiseMode) NeonRed else CyberWhite
                    ),
                    border = BorderStroke(1.dp, if (isRiseMode) NeonRed else Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(if (isRiseMode) "Rise" else "Eco", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RightGamingPanel(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val shape = remember(density) {
        GenericShape { size, _ ->
            val offset = with(density) { 60.dp.toPx() }
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(offset, size.height)
            close()
        }
    }

    Box(
        modifier = modifier
            .drawWithCache {
                val offset = 60.dp.toPx()
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height)
                    lineTo(offset, size.height)
                    close()
                }
                onDrawBehind {
                    drawPath(path, DarkGlass)
                    drawLine(
                        color = NeonRed,
                        start = Offset(0f, 0f),
                        end = Offset(offset, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            .clip(shape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {} 
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 40.dp, start = 40.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "342", color = CyberWhite, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("MHz", color = NeonRed, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0x33FF0033), CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("GPU", color = CyberWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                var isNotifiOrdinary by remember { mutableStateOf(true) }
                Button(
                    onClick = { isNotifiOrdinary = !isNotifiOrdinary },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MutedGrey.copy(alpha = 0.5f),
                        contentColor = CyberWhite
                    ),
                    border = BorderStroke(1.dp, Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(140.dp)
                ) {
                    Text(if (isNotifiOrdinary) "Ordinary Notifi" else "Block Notifi")
                }
            }

            Column(
                modifier = Modifier
                    .width(160.dp)
                    .fillMaxHeight()
                    .padding(vertical = 32.dp, horizontal = 12.dp)
            ) {
                FeatureActionTile(icon = Icons.Default.AcUnit, label = "Fan", initialActive = true)
                Spacer(modifier = Modifier.height(12.dp))
                FeatureActionTile(icon = Icons.Default.Refresh, label = "Hz", initialActive = false)
                Spacer(modifier = Modifier.height(12.dp))
                FeatureActionTile(icon = Icons.Default.Wifi, label = "WiFi", initialActive = true)
                Spacer(modifier = Modifier.height(12.dp))
                FeatureActionTile(icon = Icons.Default.Videocam, label = "Record", initialActive = false)
            }
        }
    }
}

@Composable
fun TopStatusHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.35f)
            .height(36.dp)
            .background(
                color = DarkGlass,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .drawWithCache {
                onDrawBehind {
                    drawLine(
                        color = NeonRed.copy(alpha = 0.8f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("REDMAGIC", color = NeonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("15:10", color = CyberWhite, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.BatteryFull, contentDescription = null, tint = CyberWhite, modifier = Modifier.size(16.dp))
                Text("65%", color = CyberWhite, fontSize = 12.sp)
            }
            Text("0.42 KB/S", color = CyberWhite, fontSize = 12.sp)
        }
    }
}

@Composable
fun QuickToolIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = CyberWhite,
        modifier = Modifier
            .size(40.dp)
            .background(MutedGrey.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    )
}

@Composable
fun FeatureActionTile(icon: ImageVector, label: String, initialActive: Boolean) {
    var isActive by remember { mutableStateOf(initialActive) }
    val bgColor = if (isActive) NeonRed else MutedGrey.copy(alpha = 0.8f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(bgColor, shape = RoundedCornerShape(12.dp))
            .clickable { isActive = !isActive }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = CyberWhite, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = CyberWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
