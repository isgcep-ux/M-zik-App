package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioTrackInfo
import com.example.audio.FfmpegMergeHelper
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioCyanLight
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioSurfaceCard
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioTurquoiseTint

@Composable
fun FfmpegMergeView(
  track: AudioTrackInfo,
  vocalsVol: Float,
  instrumentalVol: Float,
  isExporting: Boolean,
  exportSuccessMessage: String?,
  onExport: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val ffmpegCommand = FfmpegMergeHelper.getFfmpegCommandLine(
    vocalsGain = vocalsVol,
    instrumentalGain = instrumentalVol,
    vocalsFile = "vocals_stem.wav",
    instrumentalFile = "instrumental_stem.wav",
    outputFile = "${track.title.replace(" ", "_")}_master.mp3"
  )

  val kotlinCode = FfmpegMergeHelper.ffmpegKitKotlinSnippet

  fun copyToClipboard(text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label panoya kopyalandı", Toast.LENGTH_SHORT).show()
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("ffmpeg_merge_view"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Info Header
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
              imageVector = Icons.Default.Layers,
              contentDescription = null,
              tint = StudioCyanDark,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "FFMPEG SES BİRLEŞTİRME & EXPORT",
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = StudioTextPrimary
            )
            Text(
              text = "Vokal + Enstrümantal Stem Birleştirme Pipeline'ı",
              fontSize = 11.sp,
              color = StudioTextSecondary
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = StudioTurquoiseTint
        ) {
          Text(
            text = "320 KBPS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StudioCyanDark,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    // Success banner if exported
    if (exportSuccessMessage != null) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioTurquoiseTint),
        border = BorderStroke(1.dp, StudioGreen)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = StudioGreen,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = exportSuccessMessage,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = StudioTextPrimary
          )
        }
      }
    }

    // Live Export Action Button
    Button(
      onClick = onExport,
      enabled = !isExporting,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .shadow(6.dp, RoundedCornerShape(16.dp))
        .testTag("export_wav_button"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = StudioCyan,
        contentColor = Color.White
      )
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        if (isExporting) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color.White,
            strokeWidth = 2.dp
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "SES KANALLARI BİRLEŞTİRİLİYOR...",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        } else {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Master Parçayı Dışa Aktar (Export WAV)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }
    }

    // Section 1: FFmpeg Terminal Command
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, RoundedCornerShape(18.dp)),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
      border = BorderStroke(1.dp, StudioCardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Terminal,
              contentDescription = null,
              tint = StudioCyanDark,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "1. FFmpeg Terminal / CLI Komutu",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = StudioTextPrimary
            )
          }

          OutlinedButton(
            onClick = { copyToClipboard(ffmpegCommand, "FFmpeg Komutu") },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, StudioCyan),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = null,
              tint = StudioCyanDark,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Kopyala",
              fontSize = 11.sp,
              color = StudioCyanDark,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          shape = RoundedCornerShape(12.dp),
          color = StudioSurfaceVariant
        ) {
          Text(
            text = ffmpegCommand,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = StudioTextPrimary,
            modifier = Modifier.padding(12.dp)
          )
        }

        Text(
          text = "• [0:a]volume=1.0 Vokal kazancı, [1:a]volume=0.85 Enstrüman kazancı\n• amix=inputs=2 ile normalize edilip mikslenir\n• alimiter=limit=0.95 ile ses patlaması engellenir\n• -b:a 320k -ar 44100 stüdyo kalitesinde MP3 kodlar",
          fontSize = 11.sp,
          color = StudioTextSecondary,
          lineHeight = 16.sp
        )
      }
    }

    // Section 2: Android Kotlin Code (FFmpegKit)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, RoundedCornerShape(18.dp)),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
      border = BorderStroke(1.dp, StudioCardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Code,
              contentDescription = null,
              tint = StudioCyanDark,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "2. Android Kotlin FFmpeg Kodu",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = StudioTextPrimary
            )
          }

          OutlinedButton(
            onClick = { copyToClipboard(kotlinCode, "Android Kotlin Kodu") },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, StudioCyan),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = null,
              tint = StudioCyanDark,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Kodu Kopyala",
              fontSize = 11.sp,
              color = StudioCyanDark,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          shape = RoundedCornerShape(12.dp),
          color = StudioSurfaceVariant
        ) {
          Text(
            text = kotlinCode,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = StudioTextPrimary,
            modifier = Modifier.padding(12.dp)
          )
        }
      }
    }
  }
}
