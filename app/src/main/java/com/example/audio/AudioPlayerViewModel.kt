package com.example.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiMusicGenerator
import com.example.ai.MusicGenerationParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AudioPlayerUiState(
  val tracks: List<AudioTrackInfo> = TrackCatalog.getSampleTracks(),
  val currentTrackIndex: Int = 0,
  val isPlaying: Boolean = false,
  val currentPositionMs: Long = 0L,
  val totalDurationMs: Long = 184000L,
  val isScrubbing: Boolean = false,
  val scrubPositionMs: Long = 0L,
  val liveRmsAmplitude: Float = 0f,
  val liveFrequencyBands: List<Float> = List(8) { 0.05f },
  val volume: Float = 0.85f,
  val isMuted: Boolean = false,
  val playbackSpeed: Float = 1.0f,
  val isLooping: Boolean = false,
  val peakDecibel: Float = -12.0f,

  // App Navigation Tabs: 0 = Bestele (Prompt / Create), 1 = Çalıcı, 2 = Sözler & Akorlar, 3 = Stem Mikser, 4 = FFmpeg Export
  val selectedTab: Int = 0,

  // AI Music Generation State
  val isGeneratingMusic: Boolean = false,
  val generationStepText: String = "",
  val lastGeneratedSongTitle: String? = null,

  // Export / Merge State
  val isExporting: Boolean = false,
  val exportSuccessMessage: String? = null,

  // AI Lyrics Writer State
  val isGeneratingLyrics: Boolean = false,
  val lyricsWriterTheme: String = "Sonbahar Hüznü ve Ayrılık",
  val lyricsWriterGenre: String = "Türk Pop / Slow",
  val lyricsWriterMood: String = "Duygusal & Melankolik",
  val lyricsWriterContent: String = "",
  val lyricsWriterTitle: String = "",
  val lyricsStatusMessage: String? = null,

  // Stem Mixer Levels
  val stemVocalsVol: Float = 1.0f,
  val stemChordsVol: Float = 0.85f,
  val stemBassVol: Float = 0.90f,
  val stemRhythmVol: Float = 0.80f,
  val stemVocalsMuted: Boolean = false,
  val stemChordsMuted: Boolean = false,
  val stemBassMuted: Boolean = false,
  val stemRhythmMuted: Boolean = false
) {
  val currentTrack: AudioTrackInfo
    get() = tracks.getOrElse(currentTrackIndex) { tracks.first() }

  val effectivePositionMs: Long
    get() = if (isScrubbing) scrubPositionMs else currentPositionMs

  val playbackProgress: Float
    get() = if (totalDurationMs > 0) {
      (effectivePositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f
}

class AudioPlayerViewModel : ViewModel() {
  private val audioEngine = RealtimeAudioEngine(viewModelScope)

  private val _uiState = MutableStateFlow(AudioPlayerUiState())
  val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

  init {
    val initialTrack = _uiState.value.tracks.first()
    audioEngine.loadTrack(initialTrack)
    _uiState.update {
      it.copy(totalDurationMs = initialTrack.durationMs)
    }

    viewModelScope.launch {
      audioEngine.isPlaying.collect { playing ->
        _uiState.update { it.copy(isPlaying = playing) }
      }
    }

    viewModelScope.launch {
      audioEngine.currentPosition.collect { pos ->
        _uiState.update { current ->
          if (!current.isScrubbing) {
            current.copy(currentPositionMs = pos)
          } else current
        }
      }
    }

    viewModelScope.launch {
      audioEngine.liveRmsAmplitude.collect { rms ->
        val peakDb = if (rms > 0.001f) {
          (20f * kotlin.math.log10(rms.coerceAtLeast(0.001f))).coerceIn(-40f, -0.5f)
        } else -40f

        _uiState.update {
          it.copy(liveRmsAmplitude = rms, peakDecibel = peakDb)
        }
      }
    }

    viewModelScope.launch {
      audioEngine.liveFrequencyBands.collect { bands ->
        _uiState.update { it.copy(liveFrequencyBands = bands) }
      }
    }
  }

  fun setSelectedTab(index: Int) {
    _uiState.update { it.copy(selectedTab = index.coerceIn(0, 4)) }
  }

  fun exportCurrentTrack(context: android.content.Context) {
    if (_uiState.value.isExporting) return
    viewModelScope.launch {
      _uiState.update { it.copy(isExporting = true, exportSuccessMessage = null) }
      delay(600)
      try {
        val currentTrack = _uiState.value.currentTrack
        val exportedFile = FfmpegMergeHelper.exportSynthesizedTrackToWav(
          context = context,
          track = currentTrack,
          vocalsVol = if (_uiState.value.stemVocalsMuted) 0f else _uiState.value.stemVocalsVol,
          instrumentalVol = if (_uiState.value.stemChordsMuted) 0f else _uiState.value.stemChordsVol
        )
        _uiState.update {
          it.copy(
            isExporting = false,
            exportSuccessMessage = "Master WAV başarıyla dışa aktarıldı: ${exportedFile.name} (${exportedFile.length() / 1024} KB)"
          )
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
            isExporting = false,
            exportSuccessMessage = "Dışa aktarma hatası: ${e.localizedMessage}"
          )
        }
      }
    }
  }

  fun clearExportMessage() {
    _uiState.update { it.copy(exportSuccessMessage = null) }
  }

  fun updateLyricsTheme(theme: String) {
    _uiState.update { it.copy(lyricsWriterTheme = theme) }
  }

  fun updateLyricsGenre(genre: String) {
    _uiState.update { it.copy(lyricsWriterGenre = genre) }
  }

  fun updateLyricsMood(mood: String) {
    _uiState.update { it.copy(lyricsWriterMood = mood) }
  }

  fun updateLyricsContent(content: String) {
    _uiState.update { it.copy(lyricsWriterContent = content) }
  }

  fun updateLyricsTitle(title: String) {
    _uiState.update { it.copy(lyricsWriterTitle = title) }
  }

  fun generateLyrics(additionalNotes: String = "") {
    if (_uiState.value.isGeneratingLyrics) return

    viewModelScope.launch {
      _uiState.update {
        it.copy(
          isGeneratingLyrics = true,
          lyricsStatusMessage = "Yapay zekayla şarkı sözleri yazılıyor..."
        )
      }

      val theme = _uiState.value.lyricsWriterTheme.ifBlank { "Aşk ve Özlem" }
      val genre = _uiState.value.lyricsWriterGenre
      val mood = _uiState.value.lyricsWriterMood

      val (generatedTitle, generatedLyrics) = AiMusicGenerator.generateLyricsWithAi(
        theme = theme,
        genre = genre,
        mood = mood,
        additionalNotes = additionalNotes
      )

      _uiState.update {
        it.copy(
          isGeneratingLyrics = false,
          lyricsWriterTitle = generatedTitle,
          lyricsWriterContent = generatedLyrics,
          lyricsStatusMessage = "Sözler başarıyla oluşturuldu! Düzenleyebilir veya besteye aktarabilirsiniz."
        )
      }
    }
  }

  fun applyLyricsToCurrentTrack() {
    val currentTrack = _uiState.value.currentTrack
    val newContent = _uiState.value.lyricsWriterContent.ifBlank { return }
    val newTitle = _uiState.value.lyricsWriterTitle.ifBlank { currentTrack.title }

    // Parse sections from [TAG] format
    val parsedSections = mutableListOf<LyricSection>()
    val rawLines = newContent.lines()
    var currentTag = "BÖLÜM"
    val sectionLines = mutableListOf<String>()

    for (line in rawLines) {
      val trimmed = line.trim()
      if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
        if (sectionLines.isNotEmpty()) {
          parsedSections.add(LyricSection(currentTag, sectionLines.joinToString("\n")))
          sectionLines.clear()
        }
        currentTag = trimmed.removeSurrounding("[", "]").trim()
      } else if (trimmed.isNotBlank()) {
        sectionLines.add(line)
      }
    }
    if (sectionLines.isNotEmpty()) {
      parsedSections.add(LyricSection(currentTag, sectionLines.joinToString("\n")))
    }

    val updatedTrack = currentTrack.copy(
      title = newTitle,
      lyricsSections = if (parsedSections.isNotEmpty()) parsedSections else listOf(LyricSection("SÖZLER", newContent))
    )

    val updatedTracks = _uiState.value.tracks.toMutableList()
    val currentIndex = _uiState.value.currentTrackIndex
    if (currentIndex in updatedTracks.indices) {
      updatedTracks[currentIndex] = updatedTrack
    }

    _uiState.update {
      it.copy(
        tracks = updatedTracks,
        lyricsStatusMessage = "Sözler aktif parçaya aktarıldı!"
      )
    }
  }

  fun generateMusic(params: MusicGenerationParams) {
    if (_uiState.value.isGeneratingMusic) return

    viewModelScope.launch {
      _uiState.update {
        it.copy(
          isGeneratingMusic = true,
          generationStepText = "Müzikal tema ve ton analizi yapılıyor..."
        )
      }
      delay(400)

      _uiState.update {
        it.copy(generationStepText = "Şarkı sözleri ve vezin yazılıyor...")
      }
      delay(500)

      _uiState.update {
        it.copy(generationStepText = "Akor dizilimi ve melodi sentezleniyor...")
      }

      val newTrack = try {
        AiMusicGenerator.generateTrack(params)
      } catch (e: Exception) {
        AiMusicGenerator.generateProceduralMusic(params)
      }

      _uiState.update {
        it.copy(generationStepText = "Mastering ve ses dalgası tamamlandı!")
      }
      delay(300)

      // Add new track at the beginning of the catalog
      val updatedTracks = listOf(newTrack) + _uiState.value.tracks

      _uiState.update {
        it.copy(
          tracks = updatedTracks,
          currentTrackIndex = 0,
          totalDurationMs = newTrack.durationMs,
          currentPositionMs = 0L,
          scrubPositionMs = 0L,
          isGeneratingMusic = false,
          generationStepText = "",
          lastGeneratedSongTitle = newTrack.title,
          selectedTab = 0 // Switch to player to hear the new track
        )
      }

      audioEngine.loadTrack(newTrack)
      audioEngine.play()
    }
  }

  fun setStemVolume(stem: String, volume: Float) {
    val vol = volume.coerceIn(0f, 1.5f)
    _uiState.update { state ->
      val updated = when (stem) {
        "vocals" -> state.copy(stemVocalsVol = vol)
        "chords" -> state.copy(stemChordsVol = vol)
        "bass" -> state.copy(stemBassVol = vol)
        "rhythm" -> state.copy(stemRhythmVol = vol)
        else -> state
      }
      applyStemLevelsToEngine(updated)
      updated
    }
  }

  fun toggleStemMute(stem: String) {
    _uiState.update { state ->
      val updated = when (stem) {
        "vocals" -> state.copy(stemVocalsMuted = !state.stemVocalsMuted)
        "chords" -> state.copy(stemChordsMuted = !state.stemChordsMuted)
        "bass" -> state.copy(stemBassMuted = !state.stemBassMuted)
        "rhythm" -> state.copy(stemRhythmMuted = !state.stemRhythmMuted)
        else -> state
      }
      applyStemLevelsToEngine(updated)
      updated
    }
  }

  private fun applyStemLevelsToEngine(state: AudioPlayerUiState) {
    audioEngine.setStemLevels(
      state.stemVocalsVol,
      state.stemChordsVol,
      state.stemBassVol,
      state.stemRhythmVol
    )
    audioEngine.setStemMutes(
      state.stemVocalsMuted,
      state.stemChordsMuted,
      state.stemBassMuted,
      state.stemRhythmMuted
    )
  }

  fun togglePlayPause() {
    if (_uiState.value.isPlaying) {
      audioEngine.pause()
    } else {
      audioEngine.play()
    }
  }

  fun startScrubbing() {
    _uiState.update {
      it.copy(isScrubbing = true, scrubPositionMs = it.currentPositionMs)
    }
  }

  fun updateScrub(progress: Float) {
    val duration = _uiState.value.totalDurationMs
    val targetMs = (progress.coerceIn(0f, 1f) * duration).toLong()
    _uiState.update { it.copy(scrubPositionMs = targetMs) }
  }

  fun finishScrubbing() {
    val targetMs = _uiState.value.scrubPositionMs
    audioEngine.seekTo(targetMs)
    _uiState.update {
      it.copy(
        isScrubbing = false,
        currentPositionMs = targetMs
      )
    }
  }

  fun seekToProgress(progress: Float) {
    val duration = _uiState.value.totalDurationMs
    val targetMs = (progress.coerceIn(0f, 1f) * duration).toLong()
    audioEngine.seekTo(targetMs)
    _uiState.update { it.copy(currentPositionMs = targetMs) }
  }

  fun seekRelative(deltaMs: Long) {
    val duration = _uiState.value.totalDurationMs
    val current = _uiState.value.currentPositionMs
    val targetMs = (current + deltaMs).coerceIn(0L, duration)
    audioEngine.seekTo(targetMs)
    _uiState.update { it.copy(currentPositionMs = targetMs) }
  }

  fun selectTrack(index: Int) {
    if (index !in _uiState.value.tracks.indices) return
    val selected = _uiState.value.tracks[index]
    audioEngine.loadTrack(selected)
    _uiState.update {
      it.copy(
        currentTrackIndex = index,
        totalDurationMs = selected.durationMs,
        currentPositionMs = 0L,
        scrubPositionMs = 0L
      )
    }
  }

  fun nextTrack() {
    val nextIdx = (_uiState.value.currentTrackIndex + 1) % _uiState.value.tracks.size
    selectTrack(nextIdx)
  }

  fun previousTrack() {
    val prevIdx = if (_uiState.value.currentTrackIndex - 1 < 0) {
      _uiState.value.tracks.size - 1
    } else {
      _uiState.value.currentTrackIndex - 1
    }
    selectTrack(prevIdx)
  }

  fun setVolume(vol: Float) {
    val clamped = vol.coerceIn(0f, 1f)
    audioEngine.setVolume(clamped)
    _uiState.update { it.copy(volume = clamped, isMuted = false) }
  }

  fun toggleMute() {
    val newMute = !_uiState.value.isMuted
    audioEngine.setMuted(newMute)
    _uiState.update { it.copy(isMuted = newMute) }
  }

  fun cycleSpeed() {
    val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
    val currentSpeed = _uiState.value.playbackSpeed
    val nextIdx = (speeds.indexOf(currentSpeed) + 1) % speeds.size
    val nextSpeed = speeds[nextIdx]
    audioEngine.setPlaybackSpeed(nextSpeed)
    _uiState.update { it.copy(playbackSpeed = nextSpeed) }
  }

  fun toggleLoop() {
    val newLoop = !_uiState.value.isLooping
    audioEngine.setLooping(newLoop)
    _uiState.update { it.copy(isLooping = newLoop) }
  }

  override fun onCleared() {
    super.onCleared()
    audioEngine.stop()
  }
}
