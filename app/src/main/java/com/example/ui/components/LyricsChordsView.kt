package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioTrackInfo
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioSurfaceCard
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioTurquoiseTint

@Composable
fun LyricsChordsView(
  track: AudioTrackInfo,
  currentPositionMs: Long,
  totalDurationMs: Long,
  modifier: Modifier = Modifier
) {
  val lines: List<String> = remember(track) {
    val extracted = track.lyricsSections.flatMap { section ->
      section.lyrics.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("(") }
    }
    if (extracted.isNotEmpty()) extracted else listOf(
      "Sessiz sokaklar, solan ışıklar altında",
      "Bir şarkı çalıyor aklımda hala",
      "Son dansımız bu gece kalbimde derin yara",
      "Dönüşü yok artık giden zamana",
      "Yıldızlar şahit olsun bu sessiz vedaya",
      "Rüzgar fısıldar adını uzaklara"
    )
  }

  val chordNames: List<String> = remember(track) {
    track.chordNames.ifEmpty { listOf("Dm", "C", "Bb", "A", "Gm", "F") }
  }

  val lineCount = lines.size.coerceAtLeast(1)
  val lineDurationMs = (totalDurationMs / lineCount).coerceAtLeast(1000L)
  val activeLineIndex = ((currentPositionMs / lineDurationMs).toInt()).coerceIn(0, lineCount - 1)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("lyrics_chords_view"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Info Banner
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(StudioTurquoiseTint, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.LibraryMusic,
              contentDescription = null,
              tint = StudioCyanDark,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "ŞARKI SÖZLERİ & AKORLAR",
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = StudioTextPrimary
            )
            Text(
              text = "${track.title} • ${track.keySignature}",
              fontSize = 11.sp,
              color = StudioCyanDark
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = StudioTurquoiseTint
        ) {
          Text(
            text = "CANLI KARAOKE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StudioCyanDark,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    // Lyrics & Chords Scrollable List
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, RoundedCornerShape(20.dp)),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
      border = BorderStroke(1.dp, StudioCardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        lines.forEachIndexed { index, line ->
          val isActive = index == activeLineIndex
          val chordName = chordNames.getOrElse(index % chordNames.size) { "Dm" }

          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = if (isActive) StudioTurquoiseTint else StudioSurfaceVariant.copy(alpha = 0.5f),
            border = if (isActive) BorderStroke(1.2.dp, StudioCyan) else null
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
              // Chord Tag
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isActive) StudioCyan else StudioCardBorder
                ) {
                  Text(
                    text = chordName,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) Color.White else StudioTextSecondary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                  )
                }

                if (isActive) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = StudioCyanDark,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "ÇALINIYOR",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = StudioCyanDark
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              // Lyric line
              Text(
                text = line,
                fontSize = if (isActive) 15.sp else 14.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) StudioTextPrimary else StudioTextSecondary,
                lineHeight = 20.sp
              )
            }
          }
        }
      }
    }
  }
}
