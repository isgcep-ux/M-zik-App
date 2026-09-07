package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioCyanGlow
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioWaveformUnplayed

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
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "WaveformPulse")
  val pulseGlow by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow"
  )

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(130.dp)
      .background(
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
      )
      .border(
        width = 1.dp,
        color = com.example.ui.theme.StudioCardBorder,
        shape = RoundedCornerShape(16.dp)
      )
      .padding(horizontal = 12.dp, vertical = 10.dp)
      .testTag("waveform_visualizer")
  ) {
    val totalWidth = constraints.maxWidth.toFloat()
    val totalHeight = constraints.maxHeight.toFloat()

    // Interactive pointer input for Tap and Drag
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
          onDragEnd = {
            onFinishScrubbing()
          },
          onDragCancel = {
            onFinishScrubbing()
          }
        )
      }

    // Precalculate gradient colors
    val playedGradient = remember {
      Brush.verticalGradient(
        colors = listOf(
          StudioCyanGlow,
          StudioCyan,
          StudioViolet
        )
      )
    }

    val livePulsingGradient = remember(pulseGlow) {
      Brush.verticalGradient(
        colors = listOf(
          Color(0xFF67E8F9),
          StudioCyanGlow,
          StudioPink
        )
      )
    }

    Canvas(modifier = pointerModifier) {
      val count = waveformData.size.coerceAtLeast(1)
      val barSpacing = 2.5f.dp.toPx()
      val totalSpacing = barSpacing * (count - 1)
      val barWidth = ((totalWidth - totalSpacing) / count).coerceAtLeast(2.0f.dp.toPx())
      val centerY = totalHeight / 2f
      val maxBarHeight = totalHeight * 0.82f

      val currentScrubberX = totalWidth * playbackProgress

      for (i in 0 until count) {
        val x = i * (barWidth + barSpacing)
        val rawAmp = waveformData[i]

        // If audio is playing and we are near the playhead, slightly pulse the bar
        val distToScrubber = kotlin.math.abs(x - currentScrubberX)
        val nearPlayhead = isPlaying && distToScrubber < 40.dp.toPx()
        val dynamicBoost = if (nearPlayhead) {
          (liveRms * 0.25f * pulseGlow)
        } else 0f

        val effectiveAmp = (rawAmp + dynamicBoost).coerceIn(0.12f, 1.0f)
        val barHeight = (maxBarHeight * effectiveAmp).coerceAtLeast(6.dp.toPx())
        val topY = centerY - (barHeight / 2f)

        val isPlayed = x <= currentScrubberX

        if (isPlayed) {
          val brushToUse = if (nearPlayhead) livePulsingGradient else playedGradient
          drawRoundRect(
            brush = brushToUse,
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
        }
      }

      // Draw active Scrubber Cursor Line
      drawLine(
        color = StudioCyanGlow,
        start = Offset(currentScrubberX, 2.dp.toPx()),
        end = Offset(currentScrubberX, totalHeight - 2.dp.toPx()),
        strokeWidth = 2.dp.toPx()
      )

      // Outer glow circle on playhead
      val glowRadius = if (isPlaying || isScrubbing) 9.dp.toPx() * pulseGlow else 7.dp.toPx()
      drawCircle(
        color = StudioCyan.copy(alpha = 0.45f),
        radius = glowRadius,
        center = Offset(currentScrubberX, centerY)
      )

      // Inner white pin on playhead
      drawCircle(
        color = Color.White,
        radius = 5.dp.toPx(),
        center = Offset(currentScrubberX, centerY)
      )

      // Micro turquoise center
      drawCircle(
        color = StudioCyanDark,
        radius = 2.5f.dp.toPx(),
        center = Offset(currentScrubberX, centerY)
      )
    }

    // Show scrubbing tooltip with target timestamp
    if (isScrubbing) {
      val tooltipProgress = if (totalDurationMs > 0) {
        (scrubPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
      } else 0f
      val tooltipX = (totalWidth * tooltipProgress).toInt()

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = StudioSurfaceVariant,
        tonalElevation = 8.dp,
        modifier = Modifier
          .offset { IntOffset(x = (tooltipX - 40.dp.toPx().toInt()).coerceIn(0, (totalWidth - 80.dp.toPx()).toInt()), y = -4) }
          .align(Alignment.TopStart)
      ) {
        Text(
          text = formatTimestamp(scrubPositionMs),
          color = StudioCyanGlow,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
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
