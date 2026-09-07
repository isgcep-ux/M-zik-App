package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioSurfaceCard
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet

@Composable
fun LiveSpectrumVisualizer(
  liveFrequencyBands: List<Float>,
  liveRms: Float,
  peakDecibel: Float,
  isPlaying: Boolean,
  modifier: Modifier = Modifier
) {
  val bandLabels = listOf("60Hz", "150Hz", "400Hz", "1kHz", "2.5kHz", "6kHz", "10kHz", "16kHz")

  Column(
    modifier = modifier
      .fillMaxWidth()
      .shadow(4.dp, RoundedCornerShape(16.dp))
      .background(
        color = StudioSurfaceCard,
        shape = RoundedCornerShape(16.dp)
      )
      .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
      .padding(14.dp)
      .testTag("spectrum_visualizer")
  ) {
    // Header with Peak Meter and Limiter Status
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .width(8.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isPlaying) StudioGreen else StudioTextSecondary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isPlaying) "REAL-TIME SPECTRUM" else "SPECTRUM STANDBY",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.2.sp,
          color = if (isPlaying) StudioGreen else StudioTextSecondary
        )
      }

      // Live dBFS & Peak Limiter readout
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "PEAK: ",
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          color = StudioTextSecondary
        )
        Text(
          text = if (isPlaying) "%.1f dB".format(peakDecibel) else "-∞ dB",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = when {
            peakDecibel > -1.0f -> StudioAmber
            peakDecibel > -18.0f -> StudioCyan
            else -> StudioTextSecondary
          }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StudioCyan.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "ALIMITER: -0.5dB",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = StudioCyan
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Real-time Frequency Equalizer Bars
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      liveFrequencyBands.forEachIndexed { index, rawValue ->
        val targetHeight = if (isPlaying) rawValue.coerceIn(0.08f, 1.0f) else 0.08f
        val animatedHeight by animateFloatAsState(
          targetValue = targetHeight,
          animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
          label = "bar_$index"
        )

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(0.68f)
              .height(50.dp),
            contentAlignment = Alignment.BottomCenter
          ) {
            // Track slot background
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.04f))
            )

            // Animated vibrant bar
            val barBrush = Brush.verticalGradient(
              colors = listOf(
                StudioPink,
                StudioViolet,
                StudioCyan
              )
            )

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animatedHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(barBrush)
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = bandLabels.getOrElse(index) { "" },
            fontSize = 9.sp,
            color = StudioTextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Dual-channel (Stereo L/R) Live VU meters
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      StereoChannelMeter(channelName = "L", energy = if (isPlaying) liveRms.coerceIn(0f, 1f) else 0f, modifier = Modifier.weight(1f))
      StereoChannelMeter(channelName = "R", energy = if (isPlaying) (liveRms * 0.95f).coerceIn(0f, 1f) else 0f, modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun StereoChannelMeter(
  channelName: String,
  energy: Float,
  modifier: Modifier = Modifier
) {
  val animatedEnergy by animateFloatAsState(
    targetValue = energy,
    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
    label = "vu_$channelName"
  )

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = channelName,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = StudioTextSecondary,
      fontFamily = FontFamily.Monospace
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(Color.White.copy(alpha = 0.06f))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(animatedEnergy)
          .fillMaxHeight()
          .clip(RoundedCornerShape(3.dp))
          .background(
            Brush.horizontalGradient(
              listOf(StudioCyan, StudioViolet, StudioPink)
            )
          )
      )
    }
  }
}
