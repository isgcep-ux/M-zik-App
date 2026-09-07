package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.AiMusicGenerator
import com.example.audio.AudioPlayerViewModel
import com.example.audio.LyricSection
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackground
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
import kotlinx.coroutines.launch

@Composable
fun LyricsGeneratorScreen(
  viewModel: AudioPlayerViewModel = viewModel(),
  onNavigateToPlayer: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val scope = rememberCoroutineScope()
  val clipboardManager = LocalClipboardManager.current

  // State for user input
  var themeOrGenreInput by remember { mutableStateOf("Cyberpunk acoustic ballad about lost memories") }
  var selectedMood by remember { mutableStateOf("Melancholic & Atmospheric") }
  var generatedSongTitle by remember { mutableStateOf("") }
  var generatedLyrics by remember { mutableStateOf("") }
  var isGenerating by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isEditMode by remember { mutableStateOf(false) }
  var isCopied by remember { mutableStateOf(false) }

  // Quick Inspiration Presets
  val quickPresets = listOf(
    "Cyberpunk Synthwave",
    "Acoustic Ballad",
    "Türkçe Pop (Slow Aşk)",
    "Lo-Fi Hip Hop",
    "Indie Rock Roadtrip",
    "Late Night R&B",
    "Uplifting Hope",
    "Rainy City Nostalgia"
  )

  val moodOptions = listOf(
    "Melancholic & Atmospheric",
    "Romantic & Tender",
    "Energetic & Passionate",
    "Dreamy & Chill",
    "Hopeful & Uplifting"
  )

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(StudioBackground),
    containerColor = StudioBackground
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 18.dp, vertical = 12.dp)
        .testTag("lyrics_generator_screen"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Screen Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(StudioCyan, StudioCyanDark)
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Gemini Lyrics Studio",
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = StudioTextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = StudioTurquoiseTint
              ) {
                Text(
                  text = "GEMINI 3.5 FLASH",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = StudioCyanDark,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "AI Songwriter Engine",
                fontSize = 11.sp,
                color = StudioTextSecondary
              )
            }
          }
        }

        // Button to switch to / view full Audio Player
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = StudioSurfaceVariant,
          border = BorderStroke(1.dp, StudioCardBorder),
          modifier = Modifier.clickable { onNavigateToPlayer() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = null,
              tint = StudioCyanDark,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Player",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = StudioCyanDark
            )
          }
        }
      }

      // 2. Input Card (Theme or Genre TextField + Button)
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "SONG THEME OR GENRE",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.sp,
              color = StudioCyanDark
            )
            Text(
              text = "Input prompt for Gemini AI",
              fontSize = 11.sp,
              color = StudioTextTertiary
            )
          }

          // Main TextField for Theme or Genre
          OutlinedTextField(
            value = themeOrGenreInput,
            onValueChange = { themeOrGenreInput = it },
            placeholder = {
              Text(
                "Enter a theme or genre (e.g., Cyberpunk acoustic ballad, Türk Pop aşk acısı, Indie rock summer journey)...",
                color = StudioTextTertiary,
                fontSize = 13.sp
              )
            },
            trailingIcon = {
              if (themeOrGenreInput.isNotEmpty()) {
                IconButton(onClick = { themeOrGenreInput = "" }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = StudioTextTertiary,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("lyrics_theme_genre_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
              unfocusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.4f),
              focusedBorderColor = StudioCyan,
              unfocusedBorderColor = StudioCardBorder,
              focusedTextColor = StudioTextPrimary,
              unfocusedTextColor = StudioTextPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            maxLines = 3
          )

          // Quick Presets Carousel
          Text(
            text = "QUICK INSPIRATIONS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StudioTextSecondary,
            letterSpacing = 0.5.sp
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            quickPresets.forEach { preset ->
              val isSelected = themeOrGenreInput.contains(preset, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) StudioTurquoiseTint else StudioSurfaceVariant,
                border = BorderStroke(
                  1.dp,
                  if (isSelected) StudioCyan else Color.Transparent
                ),
                modifier = Modifier.clickable {
                  themeOrGenreInput = preset
                }
              ) {
                Text(
                  text = preset,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = if (isSelected) StudioCyanDark else StudioTextSecondary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }

          // Mood Selection Chips
          Text(
            text = "MOOD & ATMOSPHERE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StudioTextSecondary,
            letterSpacing = 0.5.sp
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            moodOptions.forEach { mood ->
              val isSelected = selectedMood == mood
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) StudioCyan else StudioSurfaceVariant,
                modifier = Modifier.clickable { selectedMood = mood }
              ) {
                Text(
                  text = mood,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else StudioTextTertiary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
              }
            }
          }

          // Gemini Generate Button
          Button(
            onClick = {
              if (themeOrGenreInput.isBlank() || isGenerating) return@Button
              scope.launch {
                isGenerating = true
                statusMessage = "Calling Gemini 3.5 Flash API..."
                isCopied = false

                try {
                  val (title, lyrics) = AiMusicGenerator.generateLyricsWithAi(
                    theme = themeOrGenreInput,
                    genre = themeOrGenreInput,
                    mood = selectedMood
                  )
                  generatedSongTitle = title
                  generatedLyrics = lyrics
                  statusMessage = "Original lyrics generated successfully!"

                  // Also update view model state so other screens stay in sync
                  viewModel.updateLyricsTheme(themeOrGenreInput)
                  viewModel.updateLyricsTitle(title)
                  viewModel.updateLyricsContent(lyrics)
                } catch (e: Exception) {
                  statusMessage = "Error generating lyrics: ${e.localizedMessage}"
                } finally {
                  isGenerating = false
                }
              }
            },
            enabled = !isGenerating && themeOrGenreInput.isNotBlank(),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("generate_lyrics_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = StudioCyan,
              disabledContainerColor = StudioCyan.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp)
          ) {
            if (isGenerating) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Gemini is Composing Lyrics...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
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
                text = "Generate Lyrics with Gemini",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
              )
            }
          }

          // Status Notice
          AnimatedVisibility(
            visible = statusMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
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

      // 3. Generated Lyrics Display Card
      AnimatedVisibility(
        visible = generatedLyrics.isNotBlank(),
        enter = fadeIn(),
        exit = fadeOut()
      ) {
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
            // Header of generated lyrics
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = generatedSongTitle.ifBlank { "Original Song" },
                  fontSize = 16.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = StudioTextPrimary
                )
                Text(
                  text = "AI Generated • $selectedMood",
                  fontSize = 11.sp,
                  color = StudioCyanDark
                )
              }

              // Actions: Edit Toggle and Copy
              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Edit toggle
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isEditMode) StudioCyan else StudioSurfaceVariant,
                  modifier = Modifier
                    .clickable { isEditMode = !isEditMode }
                    .testTag("edit_lyrics_button")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit",
                      tint = if (isEditMode) Color.White else StudioTextSecondary,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (isEditMode) "Done" else "Edit",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isEditMode) Color.White else StudioTextSecondary
                    )
                  }
                }

                // Copy button
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isCopied) StudioTurquoiseTint else StudioSurfaceVariant,
                  modifier = Modifier
                    .clickable {
                      clipboardManager.setText(AnnotatedString("$generatedSongTitle\n\n$generatedLyrics"))
                      isCopied = true
                    }
                    .testTag("copy_lyrics_button")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                      contentDescription = "Copy",
                      tint = if (isCopied) StudioCyanDark else StudioTextSecondary,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (isCopied) "Copied!" else "Copy",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isCopied) StudioCyanDark else StudioTextSecondary
                    )
                  }
                }
              }
            }

            // Quick section tag insert chips (in edit mode)
            if (isEditMode) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf("[VERSE 1]", "[PRE-CHORUS]", "[CHORUS]", "[VERSE 2]", "[BRIDGE]", "[OUTRO]").forEach { tag ->
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioTurquoiseTint,
                    modifier = Modifier.clickable {
                      generatedLyrics = "$generatedLyrics\n\n$tag\n"
                    }
                  ) {
                    Text(
                      text = tag,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = StudioCyanDark,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                  }
                }
              }
            }

            // Lyrics Text / Editor
            if (isEditMode) {
              OutlinedTextField(
                value = generatedLyrics,
                onValueChange = { generatedLyrics = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(300.dp)
                  .testTag("lyrics_content_editor"),
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
            } else {
              // Formatted presentation of lyrics
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = StudioSurfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, StudioCardBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  generatedLyrics.lines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                      Text(
                        text = trimmed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = StudioCyanDark,
                        modifier = Modifier.padding(top = 8.dp)
                      )
                    } else if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
                      Text(
                        text = trimmed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = StudioTextTertiary
                      )
                    } else {
                      Text(
                        text = line,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = StudioTextPrimary
                      )
                    }
                  }
                }
              }
            }

            // Bottom Actions: Apply to current audio track and play
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Apply lyrics to audio player
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = StudioTurquoiseTint,
                border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    viewModel.updateLyricsTitle(generatedSongTitle)
                    viewModel.updateLyricsContent(generatedLyrics)
                    viewModel.applyLyricsToCurrentTrack()
                    statusMessage = "Lyrics assigned to current music track!"
                  }
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
                    text = "Save to Track",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioCyanDark
                  )
                }
              }

              // Play / Synthesize Audio
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = StudioCyan,
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    viewModel.updateLyricsTitle(generatedSongTitle)
                    viewModel.updateLyricsContent(generatedLyrics)
                    viewModel.applyLyricsToCurrentTrack()
                    viewModel.togglePlayPause()
                    onNavigateToPlayer()
                  }
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
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Play Synthesizer",
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
}
