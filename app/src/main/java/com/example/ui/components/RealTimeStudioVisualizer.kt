package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.audio.*
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun RealTimeStudioVisualizer(
    visualizerEngine: RealTimeAudioVisualizerEngine,
    modifier: Modifier = Modifier,
    isExpandedDefault: Boolean = false
) {
    val snapshot by visualizerEngine.snapshot.collectAsState()
    val currentMode by visualizerEngine.selectedMode.collectAsState()
    val currentTheme by visualizerEngine.selectedTheme.collectAsState()

    var showFullscreenDialog by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    val themeColorPrimary = Color(currentTheme.primaryHex)
    val themeColorSecondary = Color(currentTheme.secondaryHex)
    val themeColorAccent = Color(currentTheme.accentHex)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .testTag("realtime_studio_visualizer"),
        color = FrostedGlassCardElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Visualizer Header & Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (snapshot.isPlaying) {
                                    if (snapshot.isClipping) FrostedAccentCrimson else FrostedAccentEmerald
                                } else FrostedTextMuted
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DSP REAL-TIME ANALYZER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FrostedPrimaryDark,
                            letterSpacing = 1.2.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Theme Quick Cycle
                    IconButton(
                        onClick = {
                            val nextTheme = when (currentTheme) {
                                VisualizerTheme.PRO_VIOLET -> VisualizerTheme.NEON_CYBER
                                VisualizerTheme.NEON_CYBER -> VisualizerTheme.ANALOG_GOLD
                                VisualizerTheme.ANALOG_GOLD -> VisualizerTheme.ELECTRIC_ICE
                                VisualizerTheme.ELECTRIC_ICE -> VisualizerTheme.PRO_VIOLET
                            }
                            visualizerEngine.setTheme(nextTheme)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Color Theme",
                            tint = themeColorPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Expand to Fullscreen
                    IconButton(
                        onClick = { showFullscreenDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expand Visualizer",
                            tint = FrostedPrimaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VisualizerMode.values().forEach { mode ->
                    val isSelected = (mode == currentMode)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { visualizerEngine.setMode(mode) },
                        color = if (isSelected) themeColorPrimary else FrostedGlassInput,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) themeColorPrimary else FrostedBorder
                        )
                    ) {
                        Text(
                            text = mode.title.split(" ").take(2).joinToString(" "),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSelected) Color.White else FrostedTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visualizer Canvas Container (Darkened Studio Screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F0B18))
                    .border(1.dp, Color(0xFF2A233D), RoundedCornerShape(20.dp))
            ) {
                when (currentMode) {
                    VisualizerMode.SPECTRUM_ANALYZER -> {
                        SpectrumAnalyzerCanvas(
                            snapshot = snapshot,
                            primaryColor = themeColorPrimary,
                            secondaryColor = themeColorSecondary,
                            accentColor = themeColorAccent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    VisualizerMode.OSCILLOSCOPE -> {
                        OscilloscopeCanvas(
                            snapshot = snapshot,
                            primaryColor = themeColorPrimary,
                            accentColor = themeColorAccent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    VisualizerMode.STEREO_VU_METERS -> {
                        StereoVuMetersCanvas(
                            snapshot = snapshot,
                            primaryColor = themeColorPrimary,
                            accentColor = themeColorAccent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    VisualizerMode.RADIAL_CORE -> {
                        RadialCoreCanvas(
                            snapshot = snapshot,
                            primaryColor = themeColorPrimary,
                            secondaryColor = themeColorSecondary,
                            accentColor = themeColorAccent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Small frequency label indicators at bottom
                if (currentMode == VisualizerMode.SPECTRUM_ANALYZER) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("32Hz", "125Hz", "500Hz", "2kHz", "8kHz", "16kHz").forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Studio Frequency Band Telemetry (Sub-Bass, Mid, Treble)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FrequencyMetricChip(
                    label = "SUB-BASS",
                    value = "${(snapshot.subBassEnergy * 100).toInt()}%",
                    level = snapshot.subBassEnergy,
                    color = FrostedAccentRose
                )
                FrequencyMetricChip(
                    label = "MIDRANGE",
                    value = "${(snapshot.midEnergy * 100).toInt()}%",
                    level = snapshot.midEnergy,
                    color = FrostedPrimary
                )
                FrequencyMetricChip(
                    label = "AIR/TREBLE",
                    value = "${(snapshot.trebleEnergy * 100).toInt()}%",
                    level = snapshot.trebleEnergy,
                    color = FrostedAccentEmerald
                )
            }
        }
    }

    // Fullscreen Studio Visualizer Dialog
    if (showFullscreenDialog) {
        Dialog(
            onDismissRequest = { showFullscreenDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF090611))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Dialog Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STUDIO MASTER VISUALIZER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                )
                            )
                            Text(
                                text = "${currentMode.title} • ${currentTheme.title}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f))
                            )
                        }

                        IconButton(
                            onClick = { showFullscreenDialog = false },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF1D1B20))
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode Selection Bar in Fullscreen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VisualizerMode.values().forEach { mode ->
                            val isSel = (mode == currentMode)
                            Button(
                                onClick = { visualizerEngine.setMode(mode) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) themeColorPrimary else Color(0xFF1E1A29)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = mode.title,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Immersive Large Canvas
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF07040E))
                            .border(1.5.dp, Color(0xFF282138), RoundedCornerShape(24.dp))
                    ) {
                        when (currentMode) {
                            VisualizerMode.SPECTRUM_ANALYZER -> {
                                SpectrumAnalyzerCanvas(
                                    snapshot = snapshot,
                                    primaryColor = themeColorPrimary,
                                    secondaryColor = themeColorSecondary,
                                    accentColor = themeColorAccent,
                                    modifier = Modifier.fillMaxSize(),
                                    isExpanded = true
                                )
                            }
                            VisualizerMode.OSCILLOSCOPE -> {
                                OscilloscopeCanvas(
                                    snapshot = snapshot,
                                    primaryColor = themeColorPrimary,
                                    accentColor = themeColorAccent,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            VisualizerMode.STEREO_VU_METERS -> {
                                StereoVuMetersCanvas(
                                    snapshot = snapshot,
                                    primaryColor = themeColorPrimary,
                                    accentColor = themeColorAccent,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            VisualizerMode.RADIAL_CORE -> {
                                RadialCoreCanvas(
                                    snapshot = snapshot,
                                    primaryColor = themeColorPrimary,
                                    secondaryColor = themeColorSecondary,
                                    accentColor = themeColorAccent,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Studio Palette Switcher in Fullscreen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COLOR PALETTE:",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VisualizerTheme.values().forEach { theme ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(theme.primaryHex))
                                        .border(
                                            2.dp,
                                            if (theme == currentTheme) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { visualizerEngine.setTheme(theme) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrequencyMetricChip(
    label: String,
    value: String,
    level: Float,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = FrostedGlassInput,
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
        modifier = Modifier.width(100.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FrostedTextSecondary))
                Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color))
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { level.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = Color(0xFF282337)
            )
        }
    }
}

@Composable
fun SpectrumAnalyzerCanvas(
    snapshot: VisualizerSnapshot,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    val bands = snapshot.frequencyBands
    val peakBands = snapshot.peakHoldBands

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = bands.size
        val gap = if (isExpanded) 4.dp.toPx() else 2.dp.toPx()
        val totalGaps = gap * (barCount + 1)
        val barWidth = (width - totalGaps) / barCount

        // Draw Studio Grid Lines (-48dB, -24dB, -12dB, -6dB, 0dB)
        val gridLevels = listOf(0.2f, 0.4f, 0.6f, 0.8f, 0.95f)
        gridLevels.forEach { lvl ->
            val y = height * (1f - lvl)
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw EQ Bars & Peak Markers
        for (i in 0 until barCount) {
            val bandVal = bands[i].coerceIn(0.02f, 1f)
            val peakVal = peakBands[i].coerceIn(0.02f, 1f)

            val x = gap + i * (barWidth + gap)
            val barH = height * bandVal * 0.88f
            val y = height - barH

            // Bar Gradient
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    if (bandVal > 0.85f) FrostedAccentCrimson else accentColor,
                    secondaryColor,
                    primaryColor
                ),
                startY = y,
                endY = height
            )

            // Segmented DAW Matrix style or smooth rounded bar
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 3, barWidth / 3)
            )

            // Peak Hold Floating Cap
            val peakY = height - (height * peakVal * 0.88f) - 3.dp.toPx()
            drawRoundRect(
                color = if (peakVal > 0.85f) Color(0xFFFF5252) else Color.White,
                topLeft = Offset(x, peakY.coerceAtLeast(4.dp.toPx())),
                size = Size(barWidth, 2.5.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}

@Composable
fun OscilloscopeCanvas(
    snapshot: VisualizerSnapshot,
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val points = snapshot.waveformPoints

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Draw Oscilloscope CRT reticle grid
        val gridStepX = width / 8
        for (i in 1..7) {
            val x = i * gridStepX
            drawLine(
                color = Color(0xFF1E2835),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )
        }

        val gridStepY = height / 6
        for (j in 1..5) {
            val y = j * gridStepY
            drawLine(
                color = Color(0xFF1E2835),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Center line
        drawLine(
            color = Color(0xFF334155),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.5.dp.toPx()
        )

        if (points.isEmpty()) return@Canvas

        val path = Path()
        val glowPath = Path()
        val stepX = width / (points.size - 1).coerceAtLeast(1)

        points.forEachIndexed { index, p ->
            val x = index * stepX
            val amp = p * (height * 0.42f)
            val y = centerY + amp

            if (index == 0) {
                path.moveTo(x, y)
                glowPath.moveTo(x, y)
            } else {
                path.lineTo(x, y)
                glowPath.lineTo(x, y)
            }
        }

        // Phosphor Glow Layer
        drawPath(
            path = glowPath,
            color = accentColor.copy(alpha = 0.35f),
            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Core Sharp Waveform Beam
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun StereoVuMetersCanvas(
    snapshot: VisualizerSnapshot,
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val leftLevel = snapshot.leftVuLevel.coerceIn(0f, 1.2f)
    val rightLevel = snapshot.rightVuLevel.coerceIn(0f, 1.2f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val meterWidth = (width - 24.dp.toPx()) / 2f
        val meterHeight = height * 0.85f
        val topY = height * 0.08f

        // Draw Left Channel Meter Face
        drawVuMeterFace(
            x = 8.dp.toPx(),
            y = topY,
            w = meterWidth,
            h = meterHeight,
            level = leftLevel,
            channelLabel = "LEFT (CH 1)",
            dbVal = snapshot.leftPeakDb,
            primaryColor = primaryColor
        )

        // Draw Right Channel Meter Face
        drawVuMeterFace(
            x = meterWidth + 16.dp.toPx(),
            y = topY,
            w = meterWidth,
            h = meterHeight,
            level = rightLevel,
            channelLabel = "RIGHT (CH 2)",
            dbVal = snapshot.rightPeakDb,
            primaryColor = primaryColor
        )
    }
}

fun DrawScope.drawVuMeterFace(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    level: Float,
    channelLabel: String,
    dbVal: Float,
    primaryColor: Color
) {
    // Vintage Warm Arc background
    drawRoundRect(
        color = Color(0xFF171322),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
    )

    drawRoundRect(
        color = Color(0xFF322849),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )

    // VU Arc scale
    val centerX = x + w / 2f
    val pivotY = y + h * 1.15f
    val radius = h * 0.85f

    // Draw Arc scale tick lines
    val minAngle = -50f
    val maxAngle = 50f
    val ticks = 9

    for (t in 0..ticks) {
        val frac = t.toFloat() / ticks
        val angleDeg = minAngle + frac * (maxAngle - minAngle)
        val angleRad = Math.toRadians(angleDeg.toDouble())

        val isOverload = frac > 0.75f
        val tickColor = if (isOverload) Color(0xFFFF5252) else Color(0xFFB0A3D4)

        val startR = radius * 0.88f
        val endR = radius * 0.98f

        val sx = centerX + startR * sin(angleRad).toFloat()
        val sy = pivotY - startR * cos(angleRad).toFloat()
        val ex = centerX + endR * sin(angleRad).toFloat()
        val ey = pivotY - endR * cos(angleRad).toFloat()

        drawLine(
            color = tickColor,
            start = Offset(sx, sy),
            end = Offset(ex, ey),
            strokeWidth = if (t % 2 == 0) 2.dp.toPx() else 1.dp.toPx()
        )
    }

    // Needle Angle calculation
    val needleFraction = (level / 1.0f).coerceIn(0f, 1.2f)
    val needleAngleDeg = minAngle + (needleFraction * (maxAngle - minAngle))
    val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())

    val needleLen = radius * 0.94f
    val tipX = centerX + needleLen * sin(needleAngleRad).toFloat()
    val tipY = pivotY - needleLen * cos(needleAngleRad).toFloat()

    // Needle shadow
    drawLine(
        color = Color.Black.copy(alpha = 0.4f),
        start = Offset(centerX + 2.dp.toPx(), pivotY),
        end = Offset(tipX + 2.dp.toPx(), tipY),
        strokeWidth = 2.dp.toPx()
    )

    // Needle
    val isClipping = level > 0.95f
    drawLine(
        color = if (isClipping) Color(0xFFFF5252) else Color(0xFFFFE082),
        start = Offset(centerX, pivotY),
        end = Offset(tipX, tipY),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Pivot Cap
    drawCircle(
        color = Color(0xFF4A3E68),
        radius = 8.dp.toPx(),
        center = Offset(centerX, y + h - 10.dp.toPx())
    )

    // Overload LED Indicator
    drawCircle(
        color = if (isClipping) Color(0xFFFF1744) else Color(0xFF42151B),
        radius = 4.dp.toPx(),
        center = Offset(x + w - 16.dp.toPx(), y + 16.dp.toPx())
    )
}

@Composable
fun RadialCoreCanvas(
    snapshot: VisualizerSnapshot,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val bands = snapshot.frequencyBands
    val isPlaying = snapshot.isPlaying
    val beatEnergy = snapshot.subBassEnergy

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val baseRadius = min(width, height) * 0.22f + (beatEnergy * 14.dp.toPx())

        // Pulsing background core glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f + beatEnergy * 0.3f),
                    secondaryColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 2.2f
            )
        )

        // Center Pulsing Orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, accentColor, primaryColor),
                center = Offset(centerX, centerY),
                radius = baseRadius
            ),
            radius = baseRadius,
            center = Offset(centerX, centerY)
        )

        // Radial Frequency Rays (360 degrees)
        val rayCount = bands.size
        val angleStep = 360f / rayCount

        for (i in 0 until rayCount) {
            val bandVal = bands[i].coerceIn(0.05f, 1f)
            val rayAngle = (i * angleStep + rotation) % 360f
            val rad = Math.toRadians(rayAngle.toDouble())

            val rayLength = (min(width, height) * 0.25f) * bandVal
            val startX = centerX + baseRadius * cos(rad).toFloat()
            val startY = centerY + baseRadius * sin(rad).toFloat()
            val endX = centerX + (baseRadius + rayLength) * cos(rad).toFloat()
            val endY = centerY + (baseRadius + rayLength) * sin(rad).toFloat()

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, secondaryColor, primaryColor),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
