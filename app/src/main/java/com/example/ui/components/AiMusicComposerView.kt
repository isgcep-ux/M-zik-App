package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.MusicGenerationParams
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioCyanGlow
import com.example.ui.theme.StudioCyanLight
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
fun AiMusicComposerView(
  isGenerating: Boolean,
  generationStepText: String,
  onGenerate: (MusicGenerationParams) -> Unit,
  modifier: Modifier = Modifier
) {
  val focusManager = LocalFocusManager.current

  // State matching the Suno & Producer Studio screens
  var promptLyrics by remember { mutableStateOf("") }
  var promptStyles by remember { mutableStateOf("") }
  var quickAskPrompt by remember { mutableStateOf("") }
  var isInstrumental by remember { mutableStateOf(false) }
  var isAdvancedOpen by remember { mutableStateOf(false) }

  var selectedGenre by remember { mutableStateOf("Pop") }
  var selectedMood by remember { mutableStateOf("Duygusal") }
  var selectedKey by remember { mutableStateOf("D Minor") }
  var bpmValue by remember { mutableFloatStateOf(92f) }

  val genreList = listOf("Pop", "Akustik", "Lo-Fi", "Synthwave", "Rock", "Reggae", "Ballad", "R&B")
  val moodList = listOf("Duygusal", "Melankolik", "Romantik", "Dingin", "Enerjik")
  val keyList = listOf("D Minor", "A Minor", "E Minor", "C Major", "G Major")

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("ai_music_composer_container"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Top Bar / Model Selector (Suno style: Advanced, v4.5-all, + Audio, + Voice)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Advanced Pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = StudioTurquoiseTint,
          border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.4f)),
          modifier = Modifier.clickable { isAdvancedOpen = !isAdvancedOpen }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Advanced",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = StudioCyanDark
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = StudioCyanDark,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        // Engine Version Pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = StudioSurface,
          border = BorderStroke(1.dp, StudioCardBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "v4.5-all",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = StudioTextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // + Audio Pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = StudioSurfaceVariant,
          modifier = Modifier.clickable { }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              tint = StudioTextSecondary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "Audio",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = StudioTextPrimary
            )
          }
        }

        // + Voice Pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = StudioSurfaceVariant,
          modifier = Modifier.clickable { }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              tint = StudioTextSecondary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "Voice",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = StudioTextPrimary
            )
          }
        }
      }
    }

    // 2. Inspiration Starter Cards (Screenshot 3: Brainstorm lyrics, Create a song, Remix my music)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      StarterCard(
        title = "Brainstorm lyrics",
        subtitle = "Söz ve beste fikirleri",
        badge = "AI",
        gradient = listOf(StudioCyanGlow, StudioCyan),
        onClick = {
          promptLyrics = "Yağmurlu bir İstanbul akşamında veda eden iki aşık..."
          promptStyles = "Akustik gitar, melankolik piyano, yumuşak kadın vokal"
        }
      )
      StarterCard(
        title = "Create a song together",
        subtitle = "Tarz ve melodi oluştur",
        badge = "Pop",
        gradient = listOf(StudioCyan, StudioViolet),
        onClick = {
          promptStyles = "Modern Türkçe pop, dinamik bas, 110 BPM, akılda kalıcı nakarat"
        }
      )
      StarterCard(
        title = "Remix & Merge (FFmpeg)",
        subtitle = "Stem birleştirme & mix",
        badge = "Master",
        gradient = listOf(StudioPink, StudioCyanDark),
        onClick = {
          promptStyles = "Lo-Fi chill beats, analog kaset sıcaklığı, 85 BPM"
        }
      )
    }

    // 3. Card 1: LYRICS & PROMPT CARD (Screenshot 1 & 2)
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
          .padding(16.dp)
      ) {
        // Card Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = StudioCyan,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Lyrics (Şarkı Sözleri & Prompt)",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = StudioTextPrimary
            )
          }

          // Auto-write lyrics button
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = StudioTurquoiseTint,
            modifier = Modifier.clickable {
              promptLyrics = """
[Bölüm 1]
Sessiz sokaklar, solan ışıklar altında
Bir şarkı çalıyor aklımda hala
[Nakarat]
Son dansımız bu gece kalbimde derin yara
Dönüşü yok artık giden zamana...
              """.trimIndent()
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = StudioCyanDark,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Örnek Söz Yaz",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StudioCyanDark
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-line Prompt Text Area
        OutlinedTextField(
          value = promptLyrics,
          onValueChange = { promptLyrics = it },
          placeholder = {
            Text(
              "Write lyrics or a prompt...\n(Şarkı sözlerini veya aklındaki hikayeyi buraya yaz)",
              color = StudioTextTertiary,
              fontSize = 13.sp
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("lyrics_prompt_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.5f),
            focusedBorderColor = StudioCyan,
            unfocusedBorderColor = StudioCardBorder,
            focusedTextColor = StudioTextPrimary,
            unfocusedTextColor = StudioTextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          minLines = 3,
          maxLines = 6
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Card Bottom Row: Waveform icon, Instrumental Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = null,
              tint = StudioCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isInstrumental) "Sadece Enstrümantal" else "Vokal + Enstrüman",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = StudioTextSecondary
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Instrumental",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isInstrumental) StudioCyanDark else StudioTextSecondary
            )
            Switch(
              checked = isInstrumental,
              onCheckedChange = { isInstrumental = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StudioCyan,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = StudioCardBorder
              ),
              modifier = Modifier.testTag("instrumental_switch")
            )
          }
        }
      }
    }

    // 4. Card 2: STYLES & SOUND CARD (Screenshot 1 & 2)
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
          .padding(16.dp)
      ) {
        // Card Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.MusicNote,
              contentDescription = null,
              tint = StudioCyan,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Styles & Sound (Tarz & Ses)",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = StudioTextPrimary
            )
          }

          IconButton(
            onClick = {
              val sampleStyles = listOf(
                "Acoustic pop, warm nylon guitar, mellow Rhodes piano, 90 bpm",
                "Synthwave 80s, analog Juno bass, neon drums, 120 bpm",
                "Lo-Fi chillhop, dusty vinyl crackle, soulful electric piano, 84 bpm",
                "Turkish folk pop, bağlama & acoustic guitar fusion, slow ballad",
                "Cinematic orchestral, soaring strings, emotional piano build-up"
              )
              promptStyles = sampleStyles.random()
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Rastgele Stil",
              tint = StudioCyanDark,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Style prompt input
        OutlinedTextField(
          value = promptStyles,
          onValueChange = { promptStyles = it },
          placeholder = {
            Text(
              "Describe what you want your song to sound like...\n(Örn: Akustik gitar, sıcak piyano, lo-fi davul, duygusal)",
              color = StudioTextTertiary,
              fontSize = 13.sp
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("styles_prompt_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.5f),
            focusedBorderColor = StudioCyan,
            unfocusedBorderColor = StudioCardBorder,
            focusedTextColor = StudioTextPrimary,
            unfocusedTextColor = StudioTextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          minLines = 2,
          maxLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Style Chips
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          genreList.forEach { genre ->
            val isSelected = selectedGenre == genre
            FilterChip(
              selected = isSelected,
              onClick = {
                selectedGenre = genre
                if (!promptStyles.contains(genre, ignoreCase = true)) {
                  promptStyles = if (promptStyles.isBlank()) genre else "$promptStyles, $genre"
                }
              },
              label = { Text(genre, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                containerColor = StudioSurfaceVariant,
                labelColor = StudioTextSecondary,
                selectedContainerColor = StudioCyan,
                selectedLabelColor = Color.White
              ),
              border = null,
              shape = RoundedCornerShape(8.dp)
            )
          }
        }
      }
    }

    // 5. Advanced Panel (Makam, BPM, Mood)
    AnimatedVisibility(visible = isAdvancedOpen) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
        border = BorderStroke(1.dp, StudioCardBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Gelişmiş Ayarlar (Advanced Parameters)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = StudioCyanDark
          )

          // Key selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Makam / Ton:",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = StudioTextSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              keyList.forEach { key ->
                val isSelected = selectedKey == key
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) StudioCyan else StudioSurfaceVariant,
                  modifier = Modifier.clickable { selectedKey = key }
                ) {
                  Text(
                    text = key,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else StudioTextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }

          // BPM Slider
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = StudioCyan,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Tempo: ${bpmValue.toInt()} BPM",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = StudioTextPrimary
              )
            }
          }

          Slider(
            value = bpmValue,
            onValueChange = { bpmValue = it },
            valueRange = 60f..140f,
            steps = 16,
            colors = SliderDefaults.colors(
              thumbColor = StudioCyan,
              activeTrackColor = StudioCyan,
              inactiveTrackColor = StudioCardBorder
            )
          )
        }
      }
    }

    // 6. Generating Animation Indicator
    AnimatedVisibility(visible = isGenerating) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(StudioTurquoiseTint, RoundedCornerShape(16.dp))
          .border(1.dp, StudioCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        val transition = rememberInfiniteTransition(label = "spin")
        val rotation by transition.animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
          ),
          label = "spin"
        )

        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = "Oluşturuluyor",
          tint = StudioCyanDark,
          modifier = Modifier
            .size(32.dp)
            .rotate(rotation)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = generationStepText.ifBlank { "Yapay Zeka Müzik Üretiyor..." },
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = StudioCyanDark
        )
        Text(
          text = "Sözler, polifonik akorlar ve FFmpeg ses katmanları sentezleniyor",
          fontSize = 11.sp,
          color = StudioTextSecondary
        )
      }
    }

    // 7. BIG TURQUOISE "CREATE / GENERATE" BUTTON (Screenshot 1 & 2)
    Button(
      onClick = {
        focusManager.clearFocus()
        val combinedPrompt = buildString {
          if (promptLyrics.isNotBlank()) append("Sözler: $promptLyrics. ")
          if (promptStyles.isNotBlank()) append("Tarz: $promptStyles. ")
          if (quickAskPrompt.isNotBlank()) append("İstek: $quickAskPrompt. ")
          if (isEmpty()) append("$selectedGenre tarzında, $selectedMood bir şarkı")
        }

        onGenerate(
          MusicGenerationParams(
            prompt = combinedPrompt,
            genre = selectedGenre,
            mood = selectedMood,
            bpm = bpmValue.toInt(),
            keySignature = selectedKey,
            vocalStyle = if (isInstrumental) "Enstrümantal Solo" else "Kadın Vokal"
          )
        )
      },
      enabled = !isGenerating,
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .shadow(8.dp, RoundedCornerShape(18.dp))
        .testTag("create_music_button"),
      shape = RoundedCornerShape(18.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = StudioCyan,
        contentColor = Color.White,
        disabledContainerColor = StudioCardBorder
      )
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        if (isGenerating) {
          CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = Color.White,
            strokeWidth = 2.5.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "MÜZİK OLUŞTURULUYOR...",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            letterSpacing = 1.sp
          )
        } else {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Müzik Oluştur (Create)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 0.5.sp
          )
        }
      }
    }

    // 8. Bottom "Ask Producer..." Prompt Bar (Screenshot 3)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, RoundedCornerShape(22.dp)),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
      border = BorderStroke(1.dp, StudioCardBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // '+' Action
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(StudioSurfaceVariant, CircleShape)
            .clickable { isAdvancedOpen = !isAdvancedOpen },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Ayarlar",
            tint = StudioTextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Ask Producer Input Field
        OutlinedTextField(
          value = quickAskPrompt,
          onValueChange = { quickAskPrompt = it },
          placeholder = {
            Text(
              "Ask Producer...",
              color = StudioTextTertiary,
              fontSize = 14.sp
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("ask_producer_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = StudioTextPrimary,
            unfocusedTextColor = StudioTextPrimary
          ),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions = KeyboardActions(onSend = {
            if (quickAskPrompt.isNotBlank()) {
              focusManager.clearFocus()
              onGenerate(
                MusicGenerationParams(
                  prompt = quickAskPrompt,
                  genre = selectedGenre,
                  mood = selectedMood,
                  bpm = bpmValue.toInt(),
                  keySignature = selectedKey,
                  vocalStyle = if (isInstrumental) "Enstrümantal Solo" else "Kadın Vokal"
                )
              )
            }
          }),
          singleLine = true
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Mic icon
        IconButton(
          onClick = { },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Sesli Prompt",
            tint = StudioTextTertiary,
            modifier = Modifier.size(20.dp)
          )
        }

        // Circular Turquoise Send Button
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(StudioCyan)
            .clickable {
              if (quickAskPrompt.isNotBlank() || promptStyles.isNotBlank() || promptLyrics.isNotBlank()) {
                focusManager.clearFocus()
                val finalPrompt = quickAskPrompt.ifBlank {
                  if (promptLyrics.isNotBlank()) promptLyrics else promptStyles
                }
                onGenerate(
                  MusicGenerationParams(
                    prompt = finalPrompt,
                    genre = selectedGenre,
                    mood = selectedMood,
                    bpm = bpmValue.toInt(),
                    keySignature = selectedKey,
                    vocalStyle = if (isInstrumental) "Enstrümantal Solo" else "Kadın Vokal"
                  )
                )
              }
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Gönder",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // 9. SUNO & UDIO GENERATION FEED / VARIATION CARDS
    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(StudioCyanGlow)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "SON ÜRETİLENLER (SUNO / UDIO QUEUE)",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.1.sp,
          color = StudioTextPrimary
        )
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = StudioTurquoiseTint
      ) {
        Text(
          text = "2 VARYASYON HAZIR",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = StudioCyanDark,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
    }

    // Variation A Card
    TrackVariationCard(
      title = "Sessiz Sokaklar (Master Mix)",
      versionTag = "V1 • MASTER",
      details = "Pop Ballad • 92 BPM • D Minor • 03:24",
      stems = listOf("Vokal", "Akor", "Bas", "Ritim"),
      gradient = listOf(StudioCyanGlow, StudioCyanDark)
    )

    // Variation B Card
    TrackVariationCard(
      title = "Sessiz Sokaklar (Acoustic Stripdown)",
      versionTag = "V2 • AKUSTİK",
      details = "Lo-Fi Acoustic • 88 BPM • D Minor • 03:18",
      stems = listOf("Vokal", "Akor"),
      gradient = listOf(StudioCyan, StudioViolet)
    )
  }
}

@Composable
private fun TrackVariationCard(
  title: String,
  versionTag: String,
  details: String,
  stems: List<String>,
  gradient: List<Color>
) {
  var isLocalPlaying by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(3.dp, RoundedCornerShape(18.dp)),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
    border = BorderStroke(1.dp, StudioCardBorder)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Thumbnail with gradient
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(gradient)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = StudioTurquoiseTint
            ) {
              Text(
                text = versionTag,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StudioCyanDark,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = title,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = StudioTextPrimary,
              maxLines = 1
            )
          }

          Spacer(modifier = Modifier.height(3.dp))

          Text(
            text = details,
            fontSize = 11.sp,
            color = StudioTextSecondary,
            maxLines = 1
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Stem badges
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            stems.forEach { stem ->
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = StudioSurfaceVariant
              ) {
                Text(
                  text = stem,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = StudioTextTertiary,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Circular Play / Pause action
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(StudioCyan)
          .clickable { isLocalPlaying = !isLocalPlaying },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isLocalPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = "Oynat",
          tint = Color.White,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}

@Composable
private fun StarterCard(
  title: String,
  subtitle: String,
  badge: String,
  gradient: List<Color>,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .width(180.dp)
      .clickable(onClick = onClick)
      .shadow(2.dp, RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
    border = BorderStroke(1.dp, StudioCardBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Brush.horizontalGradient(gradient))
          .padding(8.dp),
        contentAlignment = Alignment.TopStart
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color.White.copy(alpha = 0.25f)
        ) {
          Text(
            text = badge,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = StudioTextPrimary,
        maxLines = 1
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = StudioTextSecondary,
        maxLines = 1
      )
    }
  }
}
