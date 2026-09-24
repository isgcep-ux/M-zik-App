package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioCyanGlow
import com.example.ui.theme.StudioCyanLight
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioSurfaceCard
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioTurquoiseTint
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioWaveformUnplayed
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Visualizer Modes for the Dynamic Audio Waveform.
 */
enum class VisualizerMode(val label: String, val badge: String) {
  FLUID_WAVE("Akışkan Dalga", "FLUID"),
  DYNAMIC_BARS("Ritmik Çubuklar", "BARS"),
  NEON_PULSE("Neon Spektrum", "SPECTRUM")
}

@Composable
fun WaveformVisualizer(
  waveformData: List<Float>,
  playbackProgress: Float,
  isPlaying: Boolean,
  liveRms: Float,
  isScrubbing: Boolean,
  scrubPositionMs: Long,
  totalDurationMs: Long,
  onStartScrubbing: () -> Unit,
  onScrub: (Float) -> Unit,
  onFinishScrubbing: () -> Unit,
  onSeek: (Float) -> Unit,
  modifier: Modifier = Modifier,
  liveFrequencyBands: List<Float> = emptyList()
) {
  var currentMode by remember { mutableStateOf(VisualizerMode.FLUID_WAVE) }

  // Infinite animations for continuous organic wave movement
  val infiniteTransition = rememberInfiniteTransition(label = "WaveformDynamics")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wavePhase"
  )

  val pulseGlow by infiniteTransition.animateFloat(
    initialValue = 0.82f,
    targetValue = 1.22f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseGlow"
  )

  val beatScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "beatScale"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("dynamic_waveform_container"),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Mode Switcher & Real-Time Audio Telemetry HUD
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Visualizer Mode Selector Pills
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        VisualizerMode.values().forEach { mode ->
          val isSelected = currentMode == mode
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) StudioCyanDark else StudioSurfaceVariant,
            border = BorderStroke(
              1.dp,
              if (isSelected) StudioCyanDark else StudioCardBorder
            ),
            modifier = Modifier
              .clickable { currentMode = mode }
              .testTag("mode_pill_${mode.name.lowercase()}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = when (mode) {
                  VisualizerMode.FLUID_WAVE -> Icons.Default.Waves
                  VisualizerMode.DYNAMIC_BARS -> Icons.Default.GraphicEq
                  VisualizerMode.NEON_PULSE -> Icons.Default.Equalizer
                },
                contentDescription = null,
                tint = if (isSelected) Color.White else StudioCyanDark,
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = mode.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else StudioTextSecondary
              )
            }
          }
        }
      }

      // Live Audio Energy & Beat Pulse Indicator
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (isPlaying) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioTurquoiseTint,
            border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.5f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(7.dp)
                  .scale(if (isPlaying) pulseGlow else 1f)
                  .clip(CircleShape)
                  .background(StudioGreen)
              )
              Text(
                text = "CANLI %d%%".format((liveRms * 100).toInt().coerceIn(0, 100)),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = StudioCyanDark
              )
            }
          }
        } else {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = StudioSurfaceVariant
          ) {
            Text(
              text = "HAZIR",
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = StudioTextTertiary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }

    // Main Interactive Visualizer Canvas Box
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .shadow(
          elevation = if (isPlaying) 6.dp else 2.dp,
          shape = RoundedCornerShape(20.dp),
          ambientColor = StudioCyan.copy(alpha = 0.2f),
          spotColor = StudioCyanDark.copy(alpha = 0.25f)
        )
        .background(
          brush = Brush.verticalGradient(
            listOf(
              Color.White,
              StudioSurfaceVariant.copy(alpha = 0.6f)
            )
          ),
          shape = RoundedCornerShape(20.dp)
        )
        .border(
          width = 1.2.dp,
          brush = Brush.horizontalGradient(
            listOf(
              StudioCardBorder,
              if (isPlaying) StudioCyan.copy(alpha = 0.8f) else StudioCardBorder,
              StudioCardBorder
            )
          ),
          shape = RoundedCornerShape(20.dp)
        )
        .padding(horizontal = 14.dp, vertical = 10.dp)
        .testTag("waveform_visualizer")
    ) {
      val totalWidth = constraints.maxWidth.toFloat()
      val totalHeight = constraints.maxHeight.toFloat()

      // Interactive Touch & Drag Modifier for Scrubbing
      val pointerModifier = Modifier
        .fillMaxSize()
        .testTag("waveform_canvas")
        .pointerInput(Unit) {
          detectTapGestures { offset ->
            val progress = (offset.x / totalWidth).coerceIn(0f, 1f)
            onSeek(progress)
          }
        }
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { offset ->
              onStartScrubbing()
              val progress = (offset.x / totalWidth).coerceIn(0f, 1f)
              onScrub(progress)
            },
            onDrag = { change, _ ->
              change.consume()
              val progress = (change.position.x / totalWidth).coerceIn(0f, 1f)
              onScrub(progress)
            },
            onDragEnd = { onFinishScrubbing() },
            onDragCancel = { onFinishScrubbing() }
          )
        }

      // Render Visualizer Graphics
      Canvas(modifier = pointerModifier) {
        val centerY = totalHeight / 2f
        val currentScrubberX = totalWidth * playbackProgress
        val effectiveRms = if (isPlaying) liveRms.coerceIn(0.05f, 1.0f) else 0.04f

        // Draw Center Zero-Crossing Reference Line
        drawLine(
          color = StudioCardBorder.copy(alpha = 0.5f),
          start = Offset(0f, centerY),
          end = Offset(totalWidth, centerY),
          strokeWidth = 1.dp.toPx()
        )

        when (currentMode) {
          VisualizerMode.FLUID_WAVE -> {
            // Mode 1: Multi-layered Fluid Audio Waves with Glow Fill
            drawFluidWaveform(
              totalWidth = totalWidth,
              totalHeight = totalHeight,
              centerY = centerY,
              isPlaying = isPlaying,
              liveRms = effectiveRms,
              wavePhase = wavePhase,
              pulseGlow = pulseGlow,
              frequencyBands = liveFrequencyBands,
              currentScrubberX = currentScrubberX
            )
          }

          VisualizerMode.DYNAMIC_BARS -> {
            // Mode 2: Dynamic Dancing Frequency Bars reacting to live beat
            drawDynamicBars(
              waveformData = waveformData,
              totalWidth = totalWidth,
              totalHeight = totalHeight,
              centerY = centerY,
              isPlaying = isPlaying,
              liveRms = effectiveRms,
              wavePhase = wavePhase,
              pulseGlow = pulseGlow,
              currentScrubberX = currentScrubberX,
              frequencyBands = liveFrequencyBands
            )
          }

          VisualizerMode.NEON_PULSE -> {
            // Mode 3: Dual Mirrored Oscilloscope & Pulse Beam
            drawNeonPulseSpectrum(
              totalWidth = totalWidth,
              totalHeight = totalHeight,
              centerY = centerY,
              isPlaying = isPlaying,
              liveRms = effectiveRms,
              wavePhase = wavePhase,
              pulseGlow = pulseGlow,
              currentScrubberX = currentScrubberX,
              frequencyBands = liveFrequencyBands
            )
          }
        }

        // Active Playhead Scrubber Line
        drawLine(
          color = StudioCyanGlow,
          start = Offset(currentScrubberX, 2.dp.toPx()),
          end = Offset(currentScrubberX, totalHeight - 2.dp.toPx()),
          strokeWidth = 2.2.dp.toPx()
        )

        // Outer Neon Glow Halo
        val glowRadius = if (isPlaying || isScrubbing) 10.dp.toPx() * pulseGlow else 8.dp.toPx()
        drawCircle(
          color = StudioCyan.copy(alpha = if (isPlaying) 0.5f else 0.3f),
          radius = glowRadius,
          center = Offset(currentScrubberX, centerY)
        )

        // Playhead Pin Center
        drawCircle(
          color = Color.White,
          radius = 5.5f.dp.toPx(),
          center = Offset(currentScrubberX, centerY)
        )
        drawCircle(
          color = StudioCyanDark,
          radius = 3.dp.toPx(),
          center = Offset(currentScrubberX, centerY)
        )
      }

      // Timecode Scrubbing Tooltip
      if (isScrubbing) {
        val tooltipProgress = if (totalDurationMs > 0) {
          (scrubPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f
        val tooltipX = (totalWidth * tooltipProgress).toInt()

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = StudioCyanDark,
          shadowElevation = 6.dp,
          modifier = Modifier
            .offset {
              IntOffset(
                x = (tooltipX - 44.dp.toPx().toInt()).coerceIn(
                  0,
                  (totalWidth - 88.dp.toPx()).toInt()
                ),
                y = (-6).dp.toPx().toInt()
              )
            }
            .align(Alignment.TopStart)
        ) {
          Text(
            text = formatTimestamp(scrubPositionMs),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
          )
        }
      }
    }
  }
}

/**
 * Multi-layer Fluid Sine Wave Drawing
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFluidWaveform(
  totalWidth: Float,
  totalHeight: Float,
  centerY: Float,
  isPlaying: Boolean,
  liveRms: Float,
  wavePhase: Float,
  pulseGlow: Float,
  frequencyBands: List<Float>,
  currentScrubberX: Float
) {
  val stepPx = 3.5f.dp.toPx()
  val steps = (totalWidth / stepPx).toInt() + 1
  val maxAmplitude = (totalHeight * 0.42f) * (if (isPlaying) (0.45f + liveRms * 1.3f) else 0.22f)

  // Bass & Treble frequency band weights
  val bassBoost = frequencyBands.take(2).average().toFloat().coerceIn(0f, 1f)
  val trebleBoost = frequencyBands.takeLast(3).average().toFloat().coerceIn(0f, 1f)

  // 1. Background Filled Ambient Wave (Teal/Violet glow)
  val bgPath = Path()
  bgPath.moveTo(0f, centerY)

  for (i in 0..steps) {
    val x = i * stepPx
    val progress = x / totalWidth
    val taper = sin(progress * PI).toFloat() // zero at borders, 1 at center

    val wave1 = sin(progress * 14.0 + wavePhase * 1.2)
    val wave2 = sin(progress * 24.0 - wavePhase * 0.8) * 0.4
    val y = centerY + (wave1 + wave2).toFloat() * (maxAmplitude * 0.65f) * taper * (1f + bassBoost * 0.35f)

    if (i == 0) bgPath.moveTo(x, y) else bgPath.lineTo(x, y)
  }
  bgPath.lineTo(totalWidth, totalHeight)
  bgPath.lineTo(0f, totalHeight)
  bgPath.close()

  drawPath(
    path = bgPath,
    brush = Brush.verticalGradient(
      listOf(
        StudioCyan.copy(alpha = if (isPlaying) 0.22f else 0.08f),
        StudioTurquoiseTint.copy(alpha = 0.02f)
      ),
      startY = centerY,
      endY = totalHeight
    )
  )

  // 2. Secondary Harmonic Wave (Electric Violet)
  val harmonicPath = Path()
  for (i in 0..steps) {
    val x = i * stepPx
    val progress = x / totalWidth
    val taper = sin(progress * PI).toFloat()

    val wave = sin(progress * 18.0 - wavePhase * 1.5 + PI / 3.0) +
        0.3 * sin(progress * 36.0 + wavePhase * 2.0)
    val y = centerY + wave.toFloat() * (maxAmplitude * 0.50f) * taper * (1f + trebleBoost * 0.4f)

    if (i == 0) harmonicPath.moveTo(x, y) else harmonicPath.lineTo(x, y)
  }

  drawPath(
    path = harmonicPath,
    color = StudioViolet.copy(alpha = if (isPlaying) 0.55f else 0.25f),
    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
  )

  // 3. Primary Front Glowing Neon Wave (Cyan-Glow to Turquoise)
  val primaryPath = Path()
  for (i in 0..steps) {
    val x = i * stepPx
    val progress = x / totalWidth
    val taper = sin(progress * PI).toFloat()

    // Harmonic equation with organic motion
    val primaryWave = sin(progress * 11.0 + wavePhase)
    val subWave = 0.35 * sin(progress * 22.0 - wavePhase * 1.3)
    val fastWave = 0.15 * sin(progress * 44.0 + wavePhase * 2.4)
    val y = centerY + (primaryWave + subWave + fastWave).toFloat() * maxAmplitude * taper

    if (i == 0) primaryPath.moveTo(x, y) else primaryPath.lineTo(x, y)
  }

  val primaryBrush = Brush.horizontalGradient(
    listOf(
      StudioCyanDark,
      StudioCyan,
      StudioCyanGlow,
      StudioViolet,
      StudioCyan
    )
  )

  drawPath(
    path = primaryPath,
    brush = primaryBrush,
    style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
  )
}

/**
 * Dynamic Frequency Bars Mode with Real-Time Beat Oscillation
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDynamicBars(
  waveformData: List<Float>,
  totalWidth: Float,
  totalHeight: Float,
  centerY: Float,
  isPlaying: Boolean,
  liveRms: Float,
  wavePhase: Float,
  pulseGlow: Float,
  currentScrubberX: Float,
  frequencyBands: List<Float>
) {
  val count = waveformData.size.coerceAtLeast(32)
  val barSpacing = 2.4f.dp.toPx()
  val totalSpacing = barSpacing * (count - 1)
  val barWidth = ((totalWidth - totalSpacing) / count).coerceAtLeast(2.0f.dp.toPx())
  val maxBarHeight = totalHeight * 0.82f

  val playedGradient = Brush.verticalGradient(
    listOf(StudioCyanGlow, StudioCyan, StudioViolet)
  )
  val pulsingGradient = Brush.verticalGradient(
    listOf(Color(0xFF67E8F9), StudioCyanGlow, StudioPink)
  )

  for (i in 0 until count) {
    val x = i * (barWidth + barSpacing)
    val baseAmp = waveformData.getOrElse(i) { 0.35f }

    // Dynamic wave modulation per bar based on frequency bands & live RMS
    val bandIndex = i % 8
    val bandEnergy = frequencyBands.getOrElse(bandIndex) { 0.15f }

    val dynamicWave = if (isPlaying) {
      val ripple = sin(wavePhase * 2.0 + i * 0.4).toFloat() * 0.18f
      (liveRms * 0.50f + bandEnergy * 0.40f + ripple)
    } else 0f

    val distToScrubber = abs(x - currentScrubberX)
    val nearPlayhead = isPlaying && distToScrubber < 36.dp.toPx()
    val nearBoost = if (nearPlayhead) (liveRms * 0.25f * pulseGlow) else 0f

    val totalAmp = (baseAmp * 0.70f + dynamicWave + nearBoost).coerceIn(0.10f, 1.0f)
    val barHeight = (maxBarHeight * totalAmp).coerceAtLeast(5.dp.toPx())
    val topY = centerY - (barHeight / 2f)

    val isPlayed = x <= currentScrubberX

    if (isPlayed) {
      val brush = if (nearPlayhead) pulsingGradient else playedGradient
      drawRoundRect(
        brush = brush,
        topLeft = Offset(x, topY),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
      )
    } else {
      drawRoundRect(
        color = StudioWaveformUnplayed.copy(alpha = 0.65f),
        topLeft = Offset(x, topY),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
      )
      // Subtle glowing tip on unplayed bars
      if (isPlaying) {
        drawCircle(
          color = StudioCyan.copy(alpha = 0.35f),
          radius = barWidth / 2f,
          center = Offset(x + barWidth / 2f, topY)
        )
      }
    }
  }
}

/**
 * Neon Pulse Spectrum (Dual Mirrored Oscilloscope Beam)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNeonPulseSpectrum(
  totalWidth: Float,
  totalHeight: Float,
  centerY: Float,
  isPlaying: Boolean,
  liveRms: Float,
  wavePhase: Float,
  pulseGlow: Float,
  currentScrubberX: Float,
  frequencyBands: List<Float>
) {
  val stepPx = 4.dp.toPx()
  val count = (totalWidth / stepPx).toInt() + 1
  val maxH = (totalHeight * 0.40f) * (if (isPlaying) (0.35f + liveRms * 1.4f) else 0.18f)

  val topPath = Path()
  val bottomPath = Path()

  for (i in 0..count) {
    val x = i * stepPx
    val progress = x / totalWidth
    val taper = sin(progress * PI).toFloat()

    val bandIdx = (progress * 8).toInt().coerceIn(0, 7)
    val freqVal = frequencyBands.getOrElse(bandIdx) { 0.2f }

    val wave = sin(progress * 16.0 + wavePhase * 1.5) * 0.7 +
        sin(progress * 32.0 - wavePhase) * 0.3
    val deltaY = (wave.toFloat() * maxH * (1f + freqVal * 0.6f) * taper).coerceIn(2f, maxH)

    val yTop = centerY - deltaY
    val yBottom = centerY + deltaY

    if (i == 0) {
      topPath.moveTo(x, yTop)
      bottomPath.moveTo(x, yBottom)
    } else {
      topPath.lineTo(x, yTop)
      bottomPath.lineTo(x, yBottom)
    }

    // Vertical neon laser stitches on energy peaks
    if (i % 6 == 0 && isPlaying && deltaY > maxH * 0.45f) {
      drawLine(
        color = StudioCyan.copy(alpha = 0.28f),
        start = Offset(x, yTop),
        end = Offset(x, yBottom),
        strokeWidth = 1.dp.toPx()
      )
    }
  }

  // Draw Mirrored Curves
  drawPath(
    path = topPath,
    brush = Brush.horizontalGradient(listOf(StudioCyanDark, StudioCyanGlow, StudioViolet)),
    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
  )

  drawPath(
    path = bottomPath,
    brush = Brush.horizontalGradient(listOf(StudioCyanDark, StudioCyanGlow, StudioViolet)),
    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
  )
}

/**
 * Compact Animated Mini Live Waveform for Docks, Headers, and Track Selector Chips
 */
@Composable
fun MiniLiveWaveform(
  isPlaying: Boolean,
  energy: Float = 0.5f,
  modifier: Modifier = Modifier,
  barCount: Int = 5,
  barColor: Color = StudioCyanDark
) {
  val infiniteTransition = rememberInfiniteTransition(label = "mini_waveform")
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2 * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "mini_phase"
  )

  Row(
    modifier = modifier.height(16.dp),
    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 0 until barCount) {
      val animatedHeight = if (isPlaying) {
        val h = (sin(phase + i * 1.1) * 0.4 + 0.6).toFloat() * (0.35f + energy * 0.65f)
        (h * 16).coerceIn(4f, 16f)
      } else {
        4f
      }

      Box(
        modifier = Modifier
          .width(2.5.dp)
          .height(animatedHeight.dp)
          .clip(RoundedCornerShape(1.5.dp))
          .background(if (isPlaying) barColor else StudioTextTertiary.copy(alpha = 0.5f))
      )
    }
  }
}

fun formatTimestamp(ms: Long): String {
  val totalSeconds = (ms / 1000).coerceAtLeast(0)
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  val millisPart = (ms % 1000) / 100
  return "%02d:%02d.%d".format(minutes, seconds, millisPart)
}
