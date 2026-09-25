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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioCyanGlow
import com.example.ui.theme.StudioSurfaceCard
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioTurquoiseTint

/**
 * Composable screen layout for the Song Lyric Generator.
 * Includes:
 * 1. Text input for the song theme (with presets, moods, and clear action)
 * 2. Dedicated display area for AI-generated lyrics (with formatting, section highlights, and copy to clipboard)
 * 3. High-emphasis action button to trigger Gemini AI lyric generation
 */
@Composable
fun SongLyricGeneratorScreen(
  modifier: Modifier = Modifier,
  viewModel: LyricsViewModel = viewModel(),
  onNavigateToPlayer: (() -> Unit)? = null,
  onNavigateToSaved: (() -> Unit)? = null,
  onBack: (() -> Unit)? = null
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val savedLyricsList by viewModel.savedLyricsList.collectAsStateWithLifecycle()
  val clipboardManager = LocalClipboardManager.current
  var isCopied by remember { mutableStateOf(false) }
  var isEditMode by remember { mutableStateOf(false) }
  var editedLyrics by remember { mutableStateOf("") }

  LaunchedEffect(uiState.lyrics) {
    editedLyrics = uiState.lyrics
    isCopied = false
  }

  SongLyricGeneratorContent(
    theme = uiState.genreThemeInput,
    onThemeChange = { viewModel.onGenreThemeChanged(it) },
    lyrics = if (isEditMode) editedLyrics else uiState.lyrics,
    onLyricsChange = { editedLyrics = it },
    songTitle = uiState.songTitle,
    isLoading = uiState.isLoading,
    statusMessage = uiState.statusMessage,
    isSaved = uiState.isSaved,
    isEditMode = isEditMode,
    isCopied = isCopied,
    savedCount = savedLyricsList.size,
    onToggleEditMode = { isEditMode = !isEditMode },
    onGenerate = {
      isCopied = false
      viewModel.generateLyrics()
    },
    onCopy = {
      val textToCopy = if (uiState.songTitle.isNotBlank()) {
        "${uiState.songTitle}\n\n${if (isEditMode) editedLyrics else uiState.lyrics}"
      } else {
        if (isEditMode) editedLyrics else uiState.lyrics
      }
      clipboardManager.setText(AnnotatedString(textToCopy))
      isCopied = true
    },
    onSave = { viewModel.saveCurrentLyrics() },
    onNavigateToPlayer = onNavigateToPlayer,
    onNavigateToSaved = onNavigateToSaved,
    onBack = onBack,
    modifier = modifier
  )
}

/**
 * Stateless Composable layout for Song Lyric Generator.
 * Provides clean decoupling between state/ViewModel and UI presentation.
 */
@Composable
fun SongLyricGeneratorContent(
  theme: String,
  onThemeChange: (String) -> Unit,
  lyrics: String,
  onLyricsChange: (String) -> Unit = {},
  songTitle: String = "",
  isLoading: Boolean = false,
  statusMessage: String? = null,
  isSaved: Boolean = false,
  isEditMode: Boolean = false,
  isCopied: Boolean = false,
  savedCount: Int = 0,
  onToggleEditMode: () -> Unit = {},
  onGenerate: () -> Unit = {},
  onCopy: () -> Unit = {},
  onSave: () -> Unit = {},
  onNavigateToPlayer: (() -> Unit)? = null,
  onNavigateToSaved: (() -> Unit)? = null,
  onBack: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val themePresets = remember {
    listOf(
      "Cyberpunk Synthwave",
      "Acoustic Ballad",
      "Türk Pop Aşk",
      "Lo-Fi Midnight Chill",
      "Indie Rock Highway",
      "Rainy City Nostalgia",
      "Hope & Freedom",
      "Melodic Electronic"
    )
  }

  val moodOptions = remember {
    listOf(
      "Melancholic",
      "Romantic",
      "Energetic",
      "Dreamy",
      "Uplifting"
    )
  }

  var selectedMood by remember { mutableStateOf("Melancholic") }

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
        .padding(horizontal = 18.dp, vertical = 12.dp)
        .testTag("song_lyric_generator_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Top Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (onBack != null) {
            IconButton(
              onClick = onBack,
              modifier = Modifier
                .padding(end = 6.dp)
                .size(38.dp)
                .testTag("back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = StudioTextPrimary
              )
            }
          }

          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(StudioCyanGlow, StudioCyanDark)
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MusicNote,
              contentDescription = "Song Lyric Generator Logo",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Song Lyric Generator",
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
                  text = "GEMINI AI",
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
                fontWeight = FontWeight.Medium,
                color = StudioTextSecondary
              )
            }
          }
        }

        // Action Buttons on Top Bar (Saved Lyrics & Player)
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (onNavigateToSaved != null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = StudioSurfaceVariant,
              border = BorderStroke(1.dp, StudioCardBorder),
              modifier = Modifier
                .clickable { onNavigateToSaved() }
                .testTag("saved_lyrics_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Bookmark,
                  contentDescription = "Saved Lyrics",
                  tint = StudioCyanDark,
                  modifier = Modifier.size(16.dp)
                )
                if (savedCount > 0) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "$savedCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioCyanDark
                  )
                }
              }
            }
          }

          if (onNavigateToPlayer != null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = StudioSurfaceVariant,
              border = BorderStroke(1.dp, StudioCardBorder),
              modifier = Modifier
                .clickable { onNavigateToPlayer() }
                .testTag("navigate_to_player_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.GraphicEq,
                  contentDescription = "Player",
                  tint = StudioCyanDark,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "Player",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = StudioCyanDark
                )
              }
            }
          }
        }
      }

      // 2. SONG THEME INPUT SECTION (Card with Text Input & Generation Trigger)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
        border = BorderStroke(1.dp, StudioCardBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "SONG THEME",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.sp,
              color = StudioCyanDark
            )
            Text(
              text = "Prompt for AI Lyricist",
              fontSize = 11.sp,
              color = StudioTextTertiary
            )
          }

          // Main Text Input for Song Theme
          OutlinedTextField(
            value = theme,
            onValueChange = onThemeChange,
            placeholder = {
              Text(
                "Enter song theme (e.g. Cyberpunk neon rain, acoustic love ballad, summer journey)...",
                color = StudioTextTertiary,
                fontSize = 13.sp
              )
            },
            trailingIcon = {
              if (theme.isNotEmpty()) {
                IconButton(
                  onClick = { onThemeChange("") },
                  modifier = Modifier.testTag("clear_theme_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear Theme Input",
                    tint = StudioTextTertiary,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .semantics { contentDescription = "Song theme input text field" }
              .testTag("song_theme_input"),
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

          // Quick Theme Inspiration Chips
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            themePresets.forEach { preset ->
              val isSelected = theme.contains(preset, ignoreCase = true)
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) StudioTurquoiseTint else StudioSurfaceVariant,
                border = BorderStroke(
                  1.dp,
                  if (isSelected) StudioCyan else Color.Transparent
                ),
                modifier = Modifier
                  .clickable { onThemeChange(preset) }
                  .testTag("preset_$preset")
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

          // Generate Lyrics Button
          Button(
            onClick = onGenerate,
            enabled = !isLoading && theme.isNotBlank(),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("generate_lyrics_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = StudioCyan,
              disabledContainerColor = StudioCyan.copy(alpha = 0.45f)
            ),
            shape = RoundedCornerShape(14.dp)
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Gemini is Writing Lyrics...",
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
                text = "Generate Lyrics with AI",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
              )
            }
          }

          // Optional status message banner
          AnimatedVisibility(
            visible = statusMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = StudioTurquoiseTint,
              border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = statusMessage ?: "",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = StudioCyanDark,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
              )
            }
          }
        }
      }

      // 3. AI-GENERATED LYRICS DISPLAY AREA (Card with Scrollable Content)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
        border = BorderStroke(1.dp, StudioCardBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Display Area Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (songTitle.isNotBlank()) songTitle else "AI Generated Lyrics",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StudioTextPrimary
              )
              Text(
                text = if (lyrics.isNotBlank()) "Display Area • AI Studio Output" else "Waiting for input...",
                fontSize = 11.sp,
                color = StudioTextTertiary
              )
            }

            if (lyrics.isNotBlank()) {
              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Edit / Done toggle
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isEditMode) StudioCyan else StudioSurfaceVariant,
                  modifier = Modifier
                    .clickable { onToggleEditMode() }
                    .testTag("edit_lyrics_button")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit Lyrics",
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

                // Save to Room DB
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSaved) StudioTurquoiseTint else StudioSurfaceVariant,
                  border = BorderStroke(1.dp, if (isSaved) StudioCyan else Color.Transparent),
                  modifier = Modifier
                    .clickable { onSave() }
                    .testTag("save_lyrics_button")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                      contentDescription = "Save Lyrics to Database",
                      tint = if (isSaved) StudioCyanDark else StudioTextSecondary,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (isSaved) "Saved" else "Save",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSaved) StudioCyanDark else StudioTextSecondary
                    )
                  }
                }

                // Copy to Clipboard Button
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isCopied) StudioTurquoiseTint else StudioSurfaceVariant,
                  modifier = Modifier
                    .clickable { onCopy() }
                    .testTag("copy_lyrics_button")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                      contentDescription = "Copy Lyrics",
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
          }

          // Dedicated Display Area Box
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(RoundedCornerShape(12.dp))
              .background(StudioSurfaceVariant.copy(alpha = 0.35f))
              .padding(14.dp)
              .semantics { contentDescription = "AI-generated song lyrics display area" }
              .testTag("lyrics_display_area")
          ) {
            if (lyrics.isNotBlank()) {
              if (isEditMode) {
                // Editable Text Area
                OutlinedTextField(
                  value = lyrics,
                  onValueChange = onLyricsChange,
                  modifier = Modifier
                    .fillMaxSize()
                    .testTag("lyrics_editor_text_field"),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = StudioCyan,
                    unfocusedBorderColor = StudioCardBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary
                  ),
                  shape = RoundedCornerShape(10.dp)
                )
              } else {
                // Scrollable Formatted Lyrics Presentation
                val scrollState = rememberScrollState()
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(end = 44.dp)
                    .testTag("lyrics_scroll_column"),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  lyrics.lines().forEach { line ->
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

                // Floating Copy Icon in display area corner
                Surface(
                  shape = CircleShape,
                  color = if (isCopied) StudioTurquoiseTint else StudioSurfaceCard.copy(alpha = 0.92f),
                  border = BorderStroke(
                    width = 1.dp,
                    color = if (isCopied) StudioCyan else StudioCardBorder
                  ),
                  shadowElevation = 3.dp,
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                ) {
                  IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                      .size(38.dp)
                      .testTag("copy_to_clipboard_icon_button")
                  ) {
                    Icon(
                      imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                      contentDescription = "Copy to Clipboard",
                      tint = if (isCopied) StudioCyanDark else StudioTextPrimary,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            } else {
              // Empty State
              Column(
                modifier = Modifier
                  .align(Alignment.Center)
                  .testTag("lyrics_empty_state"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Box(
                  modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(StudioTurquoiseTint),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = StudioCyanDark,
                    modifier = Modifier.size(28.dp)
                  )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "No Lyrics Generated Yet",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = StudioTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Enter a song theme above and tap 'Generate Lyrics with AI'",
                  fontSize = 12.sp,
                  color = StudioTextTertiary
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Convenient aliases for the Song Lyric Generator Composable screen.
 */
@Composable
fun SongLyricGenerator(
  modifier: Modifier = Modifier,
  viewModel: LyricsViewModel = viewModel(),
  onNavigateToPlayer: (() -> Unit)? = null,
  onNavigateToSaved: (() -> Unit)? = null,
  onBack: (() -> Unit)? = null
) {
  SongLyricGeneratorScreen(
    modifier = modifier,
    viewModel = viewModel,
    onNavigateToPlayer = onNavigateToPlayer,
    onNavigateToSaved = onNavigateToSaved,
    onBack = onBack
  )
}

@Composable
fun LyricGeneratorScreen(
  modifier: Modifier = Modifier,
  viewModel: LyricsViewModel = viewModel(),
  onNavigateToPlayer: (() -> Unit)? = null,
  onNavigateToSaved: (() -> Unit)? = null,
  onBack: (() -> Unit)? = null
) {
  SongLyricGeneratorScreen(
    modifier = modifier,
    viewModel = viewModel,
    onNavigateToPlayer = onNavigateToPlayer,
    onNavigateToSaved = onNavigateToSaved,
    onBack = onBack
  )
}
