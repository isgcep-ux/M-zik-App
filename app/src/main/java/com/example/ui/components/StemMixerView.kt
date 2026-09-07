package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioSurfaceCard
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioTurquoiseTint
import com.example.ui.theme.StudioViolet

@Composable
fun StemMixerView(
  vocalsVol: Float,
  chordsVol: Float,
  bassVol: Float,
  rhythmVol: Float,
  vocalsMuted: Boolean,
  chordsMuted: Boolean,
  bassMuted: Boolean,
  rhythmMuted: Boolean,
  onVolumeChange: (stem: String, volume: Float) -> Unit,
  onToggleMute: (stem: String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("stem_mixer_view"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, RoundedCornerShape(18.dp)),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
      border = BorderStroke(1.dp, StudioCardBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "4-KANAL STEM MİKSERİ",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = StudioTextPrimary
          )
          Text(
            text = "Her enstrüman ve vokal kanalını bağımsız kontrol edin",
            fontSize = 11.sp,
            color = StudioTextSecondary
          )
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = StudioTurquoiseTint
        ) {
          Text(
            text = "CANLI MİX",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StudioCyanDark,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    // Channel 1: Vocals
    StemChannelCard(
      title = "Vokaller (Lead Vocal)",
      subtitle = "Kits.ai & ElevenLabs AI Model",
      icon = Icons.Default.Mic,
      accentColor = StudioCyan,
      volume = vocalsVol,
      isMuted = vocalsMuted,
      onVolumeChange = { onVolumeChange("vocals", it) },
      onToggleMute = { onToggleMute("vocals") }
    )

    // Channel 2: Chords & Guitars
    StemChannelCard(
      title = "Akorlar & Gitar / Piyano",
      subtitle = "Harmonik polifonik synthesizers",
      icon = Icons.Default.MusicNote,
      accentColor = StudioCyanDark,
      volume = chordsVol,
      isMuted = chordsMuted,
      onVolumeChange = { onVolumeChange("chords", it) },
      onToggleMute = { onToggleMute("chords") }
    )

    // Channel 3: Bass
    StemChannelCard(
      title = "Bas Gitar & Sub-Bass",
      subtitle = "Düşük frekans derinlik kanalı",
      icon = Icons.Default.Waves,
      accentColor = StudioViolet,
      volume = bassVol,
      isMuted = bassMuted,
      onVolumeChange = { onVolumeChange("bass", it) },
      onToggleMute = { onToggleMute("bass") }
    )

    // Channel 4: Rhythm & Drums
    StemChannelCard(
      title = "Ritim & Davul (Drums)",
      subtitle = "Kick, snare, hi-hat ve perküsyon",
      icon = Icons.Default.MusicNote,
      accentColor = StudioPink,
      volume = rhythmVol,
      isMuted = rhythmMuted,
      onVolumeChange = { onVolumeChange("rhythm", it) },
      onToggleMute = { onToggleMute("rhythm") }
    )
  }
}

@Composable
private fun StemChannelCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  volume: Float,
  isMuted: Boolean,
  onVolumeChange: (Float) -> Unit,
  onToggleMute: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(3.dp, RoundedCornerShape(18.dp)),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
    border = BorderStroke(1.dp, if (isMuted) StudioCardBorder else accentColor.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = title,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = StudioTextPrimary
            )
            Text(
              text = subtitle,
              fontSize = 10.sp,
              color = StudioTextSecondary
            )
          }
        }

        // Mute Button
        FilledTonalButton(
          onClick = onToggleMute,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isMuted) StudioAmber else StudioSurfaceVariant,
            contentColor = if (isMuted) Color.Black else StudioTextSecondary
          ),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = if (isMuted) "MUTED" else "MUTE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Slider Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = if (isMuted || volume == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
          contentDescription = null,
          tint = if (isMuted) StudioAmber else accentColor,
          modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Slider(
          value = if (isMuted) 0f else volume,
          onValueChange = onVolumeChange,
          valueRange = 0f..1.2f,
          colors = SliderDefaults.colors(
            thumbColor = accentColor,
            activeTrackColor = accentColor,
            inactiveTrackColor = StudioCardBorder
          ),
          modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = if (isMuted) "0%" else "${(volume * 100).toInt()}%",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = if (isMuted) StudioAmber else StudioTextPrimary,
          modifier = Modifier.width(42.dp)
        )
      }
    }
  }
}
