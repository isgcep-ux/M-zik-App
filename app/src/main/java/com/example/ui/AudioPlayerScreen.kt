package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.AudioPlayerUiState
import com.example.audio.AudioPlayerViewModel
import com.example.ui.components.AiLyricsWriterView
import com.example.ui.components.AiMusicComposerView
import com.example.ui.components.FfmpegMergeView
import com.example.ui.components.LiveSpectrumVisualizer
import com.example.ui.components.LyricsChordsView
import com.example.ui.components.StemMixerView
import com.example.ui.components.WaveformVisualizer
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackground
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

@Composable
fun AudioPlayerScreen(
  viewModel: AudioPlayerViewModel = viewModel(),
  onBackToLyrics: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  AudioPlayerScreen(
    viewModel = viewModel,
    uiState = uiState,
    onBackToLyrics = onBackToLyrics,
    modifier = modifier
  )
}

@Composable
fun AudioPlayerScreen(
  viewModel: AudioPlayerViewModel,
  uiState: AudioPlayerUiState,
  onBackToLyrics: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentTrack = uiState.currentTrack

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(StudioBackground),
    containerColor = StudioBackground,
    bottomBar = {
      FloatingStudioDock(
        track = currentTrack,
        isPlaying = uiState.isPlaying,
        currentPositionMs = uiState.effectivePositionMs,
        totalDurationMs = uiState.totalDurationMs,
        onTogglePlay = { viewModel.togglePlayPause() },
        onPrevTrack = { viewModel.previousTrack() },
        onNextTrack = { viewModel.nextTrack() },
        onOpenPlayer = { viewModel.setSelectedTab(1) }
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .statusBarsPadding(),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .widthIn(max = 640.dp)
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // DAW Studio Header Ribbon (Suno / Udio / Flow Music)
        StudioTopHeaderBar(
          selectedTab = uiState.selectedTab,
          onTabSelect = { viewModel.setSelectedTab(it) },
          isPlaying = uiState.isPlaying,
          onBackToLyrics = onBackToLyrics
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Screen Content by Tab
        when (uiState.selectedTab) {
          0 -> {
            // BESTELE (Prompt & Suno Music Studio)
            AiMusicComposerView(
              isGenerating = uiState.isGeneratingMusic,
              generationStepText = uiState.generationStepText,
              onGenerate = { params -> viewModel.generateMusic(params) }
            )
          }

          1 -> {
            // ÇALICI & WAVEFORM (Audio Player)
            PlayerViewContent(
              viewModel = viewModel,
              uiState = uiState
            )
          }

          2 -> {
            // SÖZ YAZMA & AKORLAR (Lyrics Writer & Chords)
            val currentLyricsContent = if (uiState.lyricsWriterContent.isNotBlank()) {
              uiState.lyricsWriterContent
            } else {
              currentTrack.fullLyricsText
            }
            val currentLyricsTitle = if (uiState.lyricsWriterTitle.isNotBlank()) {
              uiState.lyricsWriterTitle
            } else {
              currentTrack.title
            }

            AiLyricsWriterView(
              currentTrack = currentTrack,
              theme = uiState.lyricsWriterTheme,
              genre = uiState.lyricsWriterGenre,
              mood = uiState.lyricsWriterMood,
              content = currentLyricsContent,
              title = currentLyricsTitle,
              isGenerating = uiState.isGeneratingLyrics,
              statusMessage = uiState.lyricsStatusMessage,
              onThemeChange = { viewModel.updateLyricsTheme(it) },
              onGenreChange = { viewModel.updateLyricsGenre(it) },
              onMoodChange = { viewModel.updateLyricsMood(it) },
              onContentChange = { viewModel.updateLyricsContent(it) },
              onTitleChange = { viewModel.updateLyricsTitle(it) },
              onGenerate = { notes -> viewModel.generateLyrics(notes) },
              onApplyToTrack = { viewModel.applyLyricsToCurrentTrack() },
              onTransferToComposer = { title, content ->
                viewModel.setSelectedTab(0)
              }
            )
          }

          3 -> {
            // STEM MİKSERİ (4-Channel Stem Mixer)
            StemMixerView(
              vocalsVol = uiState.stemVocalsVol,
              chordsVol = uiState.stemChordsVol,
              bassVol = uiState.stemBassVol,
              rhythmVol = uiState.stemRhythmVol,
              vocalsMuted = uiState.stemVocalsMuted,
              chordsMuted = uiState.stemChordsMuted,
              bassMuted = uiState.stemBassMuted,
              rhythmMuted = uiState.stemRhythmMuted,
              onVolumeChange = { stem, vol -> viewModel.setStemVolume(stem, vol) },
              onToggleMute = { stem -> viewModel.toggleStemMute(stem) }
            )
          }

          4 -> {
            // FFMPEG BİRLEŞTİRME & KOD (Export & Code)
            FfmpegMergeView(
              track = currentTrack,
              vocalsVol = if (uiState.stemVocalsMuted) 0f else uiState.stemVocalsVol,
              instrumentalVol = if (uiState.stemChordsMuted) 0f else uiState.stemChordsVol,
              isExporting = uiState.isExporting,
              exportSuccessMessage = uiState.exportSuccessMessage,
              onExport = { viewModel.exportCurrentTrack(context) }
            )
          }
        }

        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}

/**
 * Top Studio Header Bar (Suno / Udio / Flow Music Aesthetic)
 */
@Composable
private fun StudioTopHeaderBar(
  selectedTab: Int,
  onTabSelect: (Int) -> Unit,
  isPlaying: Boolean,
  onBackToLyrics: (() -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 6.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Top Brand & Status Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Glowing Turquoise Pulse Dot
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
          initialValue = 0.8f,
          targetValue = 1.3f,
          animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
          ),
          label = "pulse_scale"
        )

        Box(
          modifier = Modifier
            .size(10.dp)
            .scale(if (isPlaying) pulseScale else 1f)
            .clip(CircleShape)
            .background(StudioCyanGlow)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = "FLOW MUSIC",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.8.sp,
          color = StudioTextPrimary
        )

        Spacer(modifier = Modifier.width(6.dp))

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = StudioTurquoiseTint
        ) {
          Text(
            text = "STUDIO AI",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = StudioCyanDark,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      // Badges: Model & Return to Lyrics Generator
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (onBackToLyrics != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = StudioCyan,
            modifier = Modifier.clickable { onBackToLyrics() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Lyrics AI",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
              )
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = StudioSurfaceCard,
          border = BorderStroke(1.dp, StudioCardBorder)
        ) {
          Text(
            text = "v4.5 PRO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = StudioTextSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = StudioTurquoiseTint,
          border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.4f))
        ) {
          Text(
            text = "💎 2,450 CR",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = StudioCyanDark,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    // Studio Workspace Segmented Tab Ribbon (DAW Style)
    val tabs = listOf(
      Triple("Bestele", Icons.Default.AutoAwesome, 0),
      Triple("Çalıcı", Icons.Default.GraphicEq, 1),
      Triple("Söz Yazma", Icons.Default.LibraryMusic, 2),
      Triple("Stems", Icons.Default.Tune, 3),
      Triple("FFmpeg", Icons.Default.Layers, 4)
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      tabs.forEach { (label, icon, index) ->
        val isSelected = selectedTab == index
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isSelected) StudioCyan else StudioSurfaceCard,
          border = BorderStroke(1.dp, if (isSelected) StudioCyan else StudioCardBorder),
          shadowElevation = if (isSelected) 3.dp else 1.dp,
          modifier = Modifier
            .clickable { onTabSelect(index) }
            .testTag("tab_$label")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = icon,
              contentDescription = label,
              tint = if (isSelected) Color.White else StudioCyanDark,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
              color = if (isSelected) Color.White else StudioTextPrimary
            )
          }
        }
      }
    }
  }
}

/**
 * Floating Studio Transport Dock (Suno / Udio / Spotify Web style floating player)
 */
@Composable
private fun FloatingStudioDock(
  track: com.example.audio.AudioTrackInfo,
  isPlaying: Boolean,
  currentPositionMs: Long,
  totalDurationMs: Long,
  onTogglePlay: () -> Unit,
  onPrevTrack: () -> Unit,
  onNextTrack: () -> Unit,
  onOpenPlayer: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .widthIn(max = 640.dp)
        .fillMaxWidth()
        .shadow(8.dp, RoundedCornerShape(22.dp))
        .clickable(onClick = onOpenPlayer)
        .testTag("floating_studio_dock"),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
      border = BorderStroke(1.2.dp, StudioCardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Artwork & Track details
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val infiniteTransition = rememberInfiniteTransition(label = "disc")
            val rotation by infiniteTransition.animateFloat(
              initialValue = 0f,
              targetValue = 360f,
              animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
              ),
              label = "disc_rotation"
            )

            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(StudioCyanGlow, StudioCyanDark)))
                .border(1.5.dp, Color.White, CircleShape)
                .rotate(if (isPlaying) rotation else 0f),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = track.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = RoundedCornerShape(5.dp),
                  color = StudioTurquoiseTint
                ) {
                  Text(
                    text = track.stemType.label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioCyanDark,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${formatTimestamp(currentPositionMs)} / ${formatTimestamp(totalDurationMs)}",
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  color = StudioTextSecondary
                )
              }
            }
          }

          // Transport controls
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = onPrevTrack,
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Önceki",
                tint = StudioTextSecondary,
                modifier = Modifier.size(22.dp)
              )
            }

            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    listOf(StudioCyanGlow, StudioCyan, StudioCyanDark)
                  )
                )
                .clickable(onClick = onTogglePlay)
                .testTag("floating_dock_play_pause"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }

            IconButton(
              onClick = onNextTrack,
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Sonraki",
                tint = StudioTextSecondary,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mini Turquoise Progress Track
        val progress = if (totalDurationMs > 0) {
          (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(StudioCardBorder)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(progress)
              .height(3.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(StudioCyan)
          )
        }
      }
    }
  }
}

/**
 * Main Player View:
 * Full interactive Waveform Visualizer, Spectrum analyzer, Transport and Volume controls.
 */
@Composable
private fun PlayerViewContent(
  viewModel: AudioPlayerViewModel,
  uiState: AudioPlayerUiState
) {
  val currentTrack = uiState.currentTrack

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Header Section
    AppHeader(track = currentTrack)

    Spacer(modifier = Modifier.height(14.dp))

    // Stem / Track Selector Chips
    StemTrackSelector(
      tracks = uiState.tracks,
      selectedIndex = uiState.currentTrackIndex,
      onSelect = { viewModel.selectTrack(it) }
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Main Card: Interactive Waveform Visualizer
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(6.dp, RoundedCornerShape(22.dp)),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
      border = BorderStroke(1.dp, StudioCardBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = "Waveform Icon",
              tint = StudioCyanDark,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "SES DALGASI (WAVEFORM)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.1.sp,
              color = StudioCyanDark
            )
          }

          Text(
            text = if (uiState.isScrubbing) "KAYDIRILIYOR..." else "DOKUN VEYA KAYDIR",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = if (uiState.isScrubbing) StudioAmber else StudioTextTertiary
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Real-time Waveform Canvas
        WaveformVisualizer(
          waveformData = currentTrack.waveformData,
          playbackProgress = uiState.playbackProgress,
          isPlaying = uiState.isPlaying,
          liveRms = uiState.liveRmsAmplitude,
          isScrubbing = uiState.isScrubbing,
          scrubPositionMs = uiState.scrubPositionMs,
          totalDurationMs = uiState.totalDurationMs,
          onStartScrubbing = { viewModel.startScrubbing() },
          onScrub = { progress -> viewModel.updateScrub(progress) },
          onFinishScrubbing = { viewModel.finishScrubbing() },
          onSeek = { progress -> viewModel.seekToProgress(progress) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Time Displays
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = formatTimestamp(uiState.effectivePositionMs),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = StudioTextPrimary
          )

          val remainingMs = (uiState.totalDurationMs - uiState.effectivePositionMs).coerceAtLeast(0L)
          Text(
            text = "-" + formatTimestamp(remainingMs),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = StudioTextSecondary
          )

          Text(
            text = formatTimestamp(uiState.totalDurationMs),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            color = StudioTextTertiary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Real-time Audio Spectrum & VU Meter
    LiveSpectrumVisualizer(
      liveFrequencyBands = uiState.liveFrequencyBands,
      liveRms = uiState.liveRmsAmplitude,
      peakDecibel = uiState.peakDecibel,
      isPlaying = uiState.isPlaying
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Audio Transport Controls
    TransportControls(
      isPlaying = uiState.isPlaying,
      playbackSpeed = uiState.playbackSpeed,
      isLooping = uiState.isLooping,
      onTogglePlayPause = { viewModel.togglePlayPause() },
      onSeekRelative = { delta -> viewModel.seekRelative(delta) },
      onNextTrack = { viewModel.nextTrack() },
      onPrevTrack = { viewModel.previousTrack() },
      onCycleSpeed = { viewModel.cycleSpeed() },
      onToggleLoop = { viewModel.toggleLoop() }
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Volume Control Section
    VolumeControlSection(
      volume = uiState.volume,
      isMuted = uiState.isMuted,
      onVolumeChange = { viewModel.setVolume(it) },
      onToggleMute = { viewModel.toggleMute() }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Audio Pipeline Info Card
    PipelineInfoCard(track = currentTrack)
  }
}

@Composable
private fun AppHeader(track: com.example.audio.AudioTrackInfo) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = StudioTurquoiseTint,
      modifier = Modifier.padding(bottom = 6.dp)
    ) {
      Text(
        text = track.stemType.label.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.3.sp,
        color = StudioCyanDark,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
      )
    }

    Text(
      text = track.title,
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = StudioTextPrimary,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )

    Spacer(modifier = Modifier.height(2.dp))

    Text(
      text = track.artist,
      style = MaterialTheme.typography.bodyMedium,
      color = StudioTextSecondary,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Tech specs line
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      SpecBadge(label = "${track.bpm} BPM")
      SpecBadge(label = track.keySignature)
      SpecBadge(label = "44.1 kHz")
      SpecBadge(label = "320k CBR")
    }
  }
}

@Composable
private fun SpecBadge(label: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(StudioSurfaceVariant)
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.SemiBold,
      color = StudioTextSecondary
    )
  }
}

@Composable
private fun StemTrackSelector(
  tracks: List<com.example.audio.AudioTrackInfo>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit
) {
  LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 4.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("stem_track_selector")
  ) {
    itemsIndexed(tracks) { index, track ->
      val isSelected = index == selectedIndex

      FilterChip(
        selected = isSelected,
        onClick = { onSelect(index) },
        label = {
          Text(
            text = track.stemType.label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = StudioTurquoiseTint,
          selectedLabelColor = StudioCyanDark,
          containerColor = StudioSurfaceVariant,
          labelColor = StudioTextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
          enabled = true,
          selected = isSelected,
          borderColor = if (isSelected) StudioCyan else Color.Transparent,
          borderWidth = 1.2.dp
        ),
        shape = RoundedCornerShape(10.dp)
      )
    }
  }
}

@Composable
private fun TransportControls(
  isPlaying: Boolean,
  playbackSpeed: Float,
  isLooping: Boolean,
  onTogglePlayPause: () -> Unit,
  onSeekRelative: (Long) -> Unit,
  onNextTrack: () -> Unit,
  onPrevTrack: () -> Unit,
  onCycleSpeed: () -> Unit,
  onToggleLoop: () -> Unit
) {
  val playButtonScale by animateFloatAsState(
    targetValue = if (isPlaying) 1.05f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    label = "play_scale"
  )

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Loop toggle
      IconButton(
        onClick = onToggleLoop,
        modifier = Modifier
          .size(48.dp)
          .testTag("loop_toggle_button")
      ) {
        Icon(
          imageVector = if (isLooping) Icons.Default.RepeatOne else Icons.Default.Repeat,
          contentDescription = "Döngüyü Aç/Kapat",
          tint = if (isLooping) StudioCyanDark else StudioTextTertiary
        )
      }

      // Previous Track
      IconButton(
        onClick = onPrevTrack,
        modifier = Modifier
          .size(48.dp)
          .testTag("previous_track_button")
      ) {
        Icon(
          imageVector = Icons.Default.SkipPrevious,
          contentDescription = "Önceki Parça",
          tint = StudioTextPrimary,
          modifier = Modifier.size(28.dp)
        )
      }

      // Rewind 5s
      IconButton(
        onClick = { onSeekRelative(-5000L) },
        modifier = Modifier
          .size(48.dp)
          .testTag("seek_backward_button")
      ) {
        Icon(
          imageVector = Icons.Default.Replay5,
          contentDescription = "5 Saniye Geri",
          tint = StudioTextPrimary,
          modifier = Modifier.size(28.dp)
        )
      }

      // Play / Pause Master Button in Turquoise
      Box(
        modifier = Modifier
          .scale(playButtonScale)
          .size(68.dp)
          .shadow(8.dp, CircleShape)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              colors = listOf(
                StudioCyanGlow,
                StudioCyan,
                StudioCyanDark
              )
            )
          )
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onTogglePlayPause
          )
          .testTag("play_pause_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = if (isPlaying) "Durdur" else "Oynat",
          tint = Color.White,
          modifier = Modifier.size(36.dp)
        )
      }

      // Next Track
      IconButton(
        onClick = onNextTrack,
        modifier = Modifier
          .size(48.dp)
          .testTag("next_track_button")
      ) {
        Icon(
          imageVector = Icons.Default.SkipNext,
          contentDescription = "Sonraki Parça",
          tint = StudioTextPrimary,
          modifier = Modifier.size(28.dp)
        )
      }

      // Speed Chip
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .clickable(onClick = onCycleSpeed)
          .testTag("speed_button"),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StudioSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "%.2fx".format(playbackSpeed).replace(".00", ""),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = StudioCyanDark
          )
        }
      }
    }
  }
}

@Composable
private fun VolumeControlSection(
  volume: Float,
  isMuted: Boolean,
  onVolumeChange: (Float) -> Unit,
  onToggleMute: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(StudioSurfaceCard, RoundedCornerShape(16.dp))
      .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("volume_section"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    IconButton(
      onClick = onToggleMute,
      modifier = Modifier
        .size(48.dp)
        .testTag("mute_toggle_button")
    ) {
      val volumeIcon = when {
        isMuted || volume == 0f -> Icons.Default.VolumeMute
        volume < 0.5f -> Icons.Default.VolumeDown
        else -> Icons.Default.VolumeUp
      }
      Icon(
        imageVector = volumeIcon,
        contentDescription = if (isMuted) "Sesi Aç" else "Sustur",
        tint = if (isMuted) StudioAmber else StudioCyanDark
      )
    }

    Slider(
      value = if (isMuted) 0f else volume,
      onValueChange = onVolumeChange,
      valueRange = 0f..1f,
      colors = SliderDefaults.colors(
        thumbColor = StudioCyan,
        activeTrackColor = StudioCyan,
        inactiveTrackColor = StudioCardBorder
      ),
      modifier = Modifier
        .weight(1f)
        .testTag("volume_slider")
    )

    Text(
      text = if (isMuted) "SESİZ" else "${(volume * 100).toInt()}%",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = StudioTextSecondary,
      modifier = Modifier.width(44.dp)
    )
  }
}

@Composable
private fun PipelineInfoCard(track: com.example.audio.AudioTrackInfo) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
    border = BorderStroke(1.dp, StudioCardBorder)
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
        Text(
          text = "PIPELINE BİLGİSİ",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.1.sp,
          color = StudioTextSecondary
        )
        Text(
          text = "FFMPEG • MASTER MIX",
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = StudioCyanDark
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = track.formatSpec,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = StudioTextPrimary
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Filtre: amix=inputs=2:normalize=0 | alimiter=limit=0.95 | 320kbps CBR",
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = StudioTextTertiary
      )
    }
  }
}
