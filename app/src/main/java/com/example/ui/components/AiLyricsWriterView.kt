package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioTrackInfo
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioCyanGlow
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceCard
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioTurquoiseTint
import com.example.ui.theme.StudioViolet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiLyricsWriterView(
  currentTrack: AudioTrackInfo,
  theme: String,
  genre: String,
  mood: String,
  content: String,
  title: String,
  isGenerating: Boolean,
  statusMessage: String?,
  onThemeChange: (String) -> Unit,
  onGenreChange: (String) -> Unit,
  onMoodChange: (String) -> Unit,
  onContentChange: (String) -> Unit,
  onTitleChange: (String) -> Unit,
  onGenerate: (String) -> Unit,
  onApplyToTrack: () -> Unit,
  onTransferToComposer: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  var copiedToClipboard by remember { mutableStateOf(false) }
  var additionalNotes by remember { mutableStateOf("") }
  var selectedTab by remember { mutableStateOf(0) } // 0 = Söz Yazarı / Editör, 1 = Canlı Karaoke & Akorlar

  val genrePresets = listOf("Türk Pop", "Slow Ballad", "Akustik", "Lo-Fi", "Rock", "R&B", "Trap")
  val moodPresets = listOf("Duygusal", "Melankolik", "Tutkulu", "Hüzünlü", "Umut Dolu", "Enerjik")
  val themePresets = listOf(
    "Ayrılık ve Yağmur",
    "Son Dansımız",
    "Yeniden Doğuş",
    "Gece Yolculuğu",
    "Sessiz Sokaklar",
    "Yıldızlar ve Veda"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("ai_lyrics_writer_container"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Üst Sekme Seçici: Söz Yazma / Editör vs Canlı Karaoke & Akorlar
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = StudioSurfaceCard,
      border = BorderStroke(1.dp, StudioCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (selectedTab == 0) StudioCyan else Color.Transparent,
          modifier = Modifier
            .weight(1f)
            .clickable { selectedTab = 0 }
            .testTag("lyrics_tab_write")
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.EditNote,
              contentDescription = null,
              tint = if (selectedTab == 0) Color.White else StudioCyanDark,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Yapay Zeka Söz Yazarı",
              fontSize = 12.sp,
              fontWeight = if (selectedTab == 0) FontWeight.ExtraBold else FontWeight.SemiBold,
              color = if (selectedTab == 0) Color.White else StudioTextPrimary
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (selectedTab == 1) StudioCyan else Color.Transparent,
          modifier = Modifier
            .weight(1f)
            .clickable { selectedTab = 1 }
            .testTag("lyrics_tab_karaoke")
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LibraryMusic,
              contentDescription = null,
              tint = if (selectedTab == 1) Color.White else StudioCyanDark,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Canlı Karaoke & Akor",
              fontSize = 12.sp,
              fontWeight = if (selectedTab == 1) FontWeight.ExtraBold else FontWeight.SemiBold,
              color = if (selectedTab == 1) Color.White else StudioTextPrimary
            )
          }
        }
      }
    }

    if (selectedTab == 1) {
      // Canlı Karaoke Görünümü
      LyricsChordsView(
        track = currentTrack,
        currentPositionMs = 0L,
        totalDurationMs = currentTrack.durationMs
      )
    } else {
      // 2. Yapay Zeka Söz Parametreleri Kartı (Tema, Tür, Duygu)
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
          // Kart Başlığı
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Brush.linearGradient(listOf(StudioCyanGlow, StudioCyanDark))),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Psychology,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "AI SÖZ VE ŞİİR YAZARI",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 1.sp,
                  color = StudioTextPrimary
                )
                Text(
                  text = "Tema & türe özel ritmik, kafiyeli şarkı sözü motoru",
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
                text = "GEMINI 3.5 FLASH",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = StudioCyanDark,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }

          // A. Tema Giriş Alanı
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "ŞARKI TEMASI VEYA HİKAYESİ",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp,
              color = StudioCyanDark
            )

            OutlinedTextField(
              value = theme,
              onValueChange = onThemeChange,
              placeholder = {
                Text(
                  "Örn: Yağmurlu gecede ayrılan iki sevgilinin hüznü...",
                  color = StudioTextTertiary,
                  fontSize = 13.sp
                )
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("lyrics_theme_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
                unfocusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
                focusedBorderColor = StudioCyan,
                unfocusedBorderColor = StudioCardBorder,
                focusedTextColor = StudioTextPrimary,
                unfocusedTextColor = StudioTextPrimary
              ),
              shape = RoundedCornerShape(14.dp),
              singleLine = true
            )

            // Hızlı Tema Etiketleri
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              themePresets.forEach { preset ->
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (theme == preset) StudioTurquoiseTint else StudioSurfaceVariant,
                  border = BorderStroke(
                    1.dp,
                    if (theme == preset) StudioCyan else Color.Transparent
                  ),
                  modifier = Modifier.clickable { onThemeChange(preset) }
                ) {
                  Text(
                    text = preset,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (theme == preset) StudioCyanDark else StudioTextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }

          // B. Tür & Ruh Hali Seçimi
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Müzik Türü
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "MÜZİK TÜRÜ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StudioCyanDark
              )

              OutlinedTextField(
                value = genre,
                onValueChange = onGenreChange,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
                  unfocusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
                  focusedBorderColor = StudioCyan,
                  unfocusedBorderColor = StudioCardBorder,
                  focusedTextColor = StudioTextPrimary,
                  unfocusedTextColor = StudioTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
              )
            }

            // Ruh Hali
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "RUH HALİ / DUYGU",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StudioCyanDark
              )

              OutlinedTextField(
                value = mood,
                onValueChange = onMoodChange,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
                  unfocusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
                  focusedBorderColor = StudioCyan,
                  unfocusedBorderColor = StudioCardBorder,
                  focusedTextColor = StudioTextPrimary,
                  unfocusedTextColor = StudioTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
              )
            }
          }

          // C. Hızlı Tür Hapları
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            genrePresets.forEach { g ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (genre == g) StudioCyan else StudioSurfaceVariant,
                modifier = Modifier.clickable { onGenreChange(g) }
              ) {
                Text(
                  text = g,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (genre == g) Color.White else StudioTextTertiary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          // D. Söz Üret Butonu
          Button(
            onClick = { onGenerate(additionalNotes) },
            enabled = !isGenerating,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("lyrics_generate_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = StudioCyan,
              disabledContainerColor = StudioCyan.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(14.dp)
          ) {
            if (isGenerating) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Yapay Zeka Yazıyor...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            } else {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Yapay Zeka ile Sözleri Oluştur",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
              )
            }
          }

          // Durum Mesajı
          AnimatedVisibility(visible = statusMessage != null) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = StudioTurquoiseTint,
              border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = statusMessage ?: "",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = StudioCyanDark,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
            }
          }
        }
      }

      // 3. Şarkı Sözü Metin Editörü Kartı (Bölüm Etiketli & Düzenlenebilir)
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
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Editör Üst Araç Çubuğu
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                tint = StudioCyan,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "SÖZ EDİTÖRÜ",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = StudioTextPrimary
              )
            }

            // Hızlı Eylemler (Kopyala, Temizle, Hızlı Bölüm Ekle)
            Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = {
                  if (content.isNotBlank()) {
                    clipboardManager.setText(AnnotatedString(content))
                    copiedToClipboard = true
                  }
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = if (copiedToClipboard) Icons.Default.Check else Icons.Default.ContentCopy,
                  contentDescription = "Kopyala",
                  tint = if (copiedToClipboard) StudioCyan else StudioTextSecondary,
                  modifier = Modifier.size(16.dp)
                )
              }

              IconButton(
                onClick = { onContentChange("") },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = "Temizle",
                  tint = StudioTextSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          // Şarkı Başlığı Alanı
          OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("Şarkı Başlığı (Örn: Sessiz Sokaklar)", color = StudioTextTertiary, fontSize = 13.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("lyrics_editor_title"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
              unfocusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
              focusedBorderColor = StudioCyan,
              unfocusedBorderColor = StudioCardBorder,
              focusedTextColor = StudioTextPrimary,
              unfocusedTextColor = StudioTextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = StudioCyanDark,
                modifier = Modifier.size(16.dp)
              )
            }
          )

          // Hızlı Bölüm Ekleme Butonları ([GİRİŞ], [NAKARAT], [KÖPRÜ], [ÇIKIŞ])
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val sectionTags = listOf("[GİRİŞ]", "[BÖLÜM 1]", "[ÖN NAKARAT]", "[NAKARAT]", "[BÖLÜM 2]", "[KÖPRÜ]", "[ÇIKIŞ]")
            sectionTags.forEach { tag ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = StudioTurquoiseTint,
                modifier = Modifier.clickable {
                  val newText = if (content.isBlank()) "$tag\n" else "$content\n\n$tag\n"
                  onContentChange(newText)
                }
              ) {
                Text(
                  text = tag,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = StudioCyanDark,
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
              }
            }
          }

          // Metin Editörü Alanı (Büyük ve Çok Satırlı)
          OutlinedTextField(
            value = content,
            onValueChange = {
              onContentChange(it)
              copiedToClipboard = false
            },
            placeholder = {
              Text(
                "Yapay zeka ile üretilen sözler burada belirecek.\nİstediğin gibi değiştirebilir, silebilir veya yeni dizeler ekleyebilirsin...",
                color = StudioTextTertiary,
                fontSize = 13.sp,
                lineHeight = 18.sp
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp)
              .testTag("lyrics_editor_content"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.3f),
              unfocusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.3f),
              focusedBorderColor = StudioCyan,
              unfocusedBorderColor = StudioCardBorder,
              focusedTextColor = StudioTextPrimary,
              unfocusedTextColor = StudioTextPrimary
            ),
            shape = RoundedCornerShape(14.dp)
          )

          // Editör Aksiyon Butonları (Aktif Parçaya Kaydet & Bestele Sekmesine Gönder)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Aktif Parçaya Kaydet
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = StudioTurquoiseTint,
              border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.4f)),
              modifier = Modifier
                .weight(1f)
                .clickable { onApplyToTrack() }
                .testTag("lyrics_apply_to_track_button")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Save,
                  contentDescription = null,
                  tint = StudioCyanDark,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Parçaya Kaydet",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = StudioCyanDark
                )
              }
            }

            // Bestele Sekmesine Aktar
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = StudioCyan,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  onTransferToComposer(title, content)
                }
                .testTag("lyrics_transfer_composer_button")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Beste Yap (Suno)",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              }
            }
          }
        }
      }
    }
  }
}
