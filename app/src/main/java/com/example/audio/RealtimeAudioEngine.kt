package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

class RealtimeAudioEngine(
  private val scope: CoroutineScope
) {
  private val sampleRate = 44100
  private val bufferSize = AudioTrack.getMinBufferSize(
    sampleRate,
    AudioFormat.CHANNEL_OUT_MONO,
    AudioFormat.ENCODING_PCM_16BIT
  ).coerceAtLeast(4096)

  private var audioTrack: AudioTrack? = null
  private var playbackJob: Job? = null

  private var currentTrack: AudioTrackInfo? = null
  private var positionMs: Long = 0L
  private var durationMs: Long = 180000L
  private var isPlayingState: Boolean = false
  private var playbackSpeed: Float = 1.0f
  private var volumeLevel: Float = 0.85f
  private var isMuted: Boolean = false
  private var isLooping: Boolean = false

  // Stem Volume Multipliers (0.0 to 1.5)
  private var stemVocalsLevel: Float = 1.0f
  private var stemChordsLevel: Float = 0.85f
  private var stemBassLevel: Float = 0.90f
  private var stemRhythmLevel: Float = 0.80f

  // Stem Mute Flags
  private var stemVocalsMuted: Boolean = false
  private var stemChordsMuted: Boolean = false
  private var stemBassMuted: Boolean = false
  private var stemRhythmMuted: Boolean = false

  // Emits real-time live spectral bands [0..1]
  private val _liveFrequencyBands = MutableStateFlow(List(8) { 0.05f })
  val liveFrequencyBands = _liveFrequencyBands.asStateFlow()

  // Emits real-time instantaneous RMS amplitude [0..1]
  private val _liveRmsAmplitude = MutableStateFlow(0f)
  val liveRmsAmplitude = _liveRmsAmplitude.asStateFlow()

  // Current position Flow
  private val _currentPosition = MutableStateFlow(0L)
  val currentPosition = _currentPosition.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying = _isPlaying.asStateFlow()

  fun loadTrack(track: AudioTrackInfo, startPositionMs: Long = 0L) {
    val wasPlaying = isPlayingState
    stop()
    currentTrack = track
    durationMs = track.durationMs
    positionMs = startPositionMs.coerceIn(0L, durationMs)
    _currentPosition.value = positionMs
    if (wasPlaying) {
      play()
    }
  }

  fun setStemLevels(vocals: Float, chords: Float, bass: Float, rhythm: Float) {
    stemVocalsLevel = vocals.coerceIn(0f, 1.5f)
    stemChordsLevel = chords.coerceIn(0f, 1.5f)
    stemBassLevel = bass.coerceIn(0f, 1.5f)
    stemRhythmLevel = rhythm.coerceIn(0f, 1.5f)
  }

  fun setStemMutes(vocalsMute: Boolean, chordsMute: Boolean, bassMute: Boolean, rhythmMute: Boolean) {
    stemVocalsMuted = vocalsMute
    stemChordsMuted = chordsMute
    stemBassMuted = bassMute
    stemRhythmMuted = rhythmMute
  }

  fun play() {
    if (isPlayingState) return
    isPlayingState = true
    _isPlaying.value = true

    initAudioTrack()

    playbackJob = scope.launch(Dispatchers.Default) {
      try {
        audioTrack?.play()
      } catch (e: Exception) {
        Log.e("RealtimeAudioEngine", "AudioTrack play error", e)
      }

      val buffer = ShortArray(bufferSize / 2)
      var sampleIndex = (positionMs * sampleRate / 1000L).toDouble()

      val trackBpm = currentTrack?.bpm ?: 90
      val beatDurationSec = 60.0 / trackBpm
      val chordDurationSec = beatDurationSec * 4.0 // 1 chord per 4 beats (measure)
      val chords = currentTrack?.chordProgression?.ifEmpty { null }
        ?: listOf(floatArrayOf(146.83f, 293.66f, 349.23f, 440.0f))

      var lastPositionUpdateTime = System.currentTimeMillis()
      val random = java.util.Random(1337)

      while (isActive && isPlayingState) {
        val now = System.currentTimeMillis()
        val dt = now - lastPositionUpdateTime
        lastPositionUpdateTime = now

        positionMs += (dt * playbackSpeed).toLong()
        if (positionMs >= durationMs) {
          if (isLooping) {
            positionMs = 0L
            sampleIndex = 0.0
          } else {
            positionMs = durationMs
            _currentPosition.value = positionMs
            _isPlaying.value = false
            isPlayingState = false
            resetLiveMeters()
            break
          }
        }
        _currentPosition.value = positionMs

        val masterVol = if (isMuted) 0f else volumeLevel
        val effVocals = if (stemVocalsMuted) 0f else stemVocalsLevel * masterVol
        val effChords = if (stemChordsMuted) 0f else stemChordsLevel * masterVol
        val effBass = if (stemBassMuted) 0f else stemBassLevel * masterVol
        val effRhythm = if (stemRhythmMuted) 0f else stemRhythmLevel * masterVol

        var sumSquares = 0.0

        for (i in buffer.indices) {
          val t = (sampleIndex + i) / sampleRate
          val currentSec = t % (durationMs / 1000.0)

          // 1. Chords & Harmony
          val chordIdx = ((currentSec / chordDurationSec).toInt()) % chords.size
          val activeNotes = chords[chordIdx]

          var chordsWave = 0.0
          if (effChords > 0f) {
            for ((noteIdx, freq) in activeNotes.withIndex()) {
              if (freq < 150f) continue // Skip sub-bass in mid-chords
              val fundamental = sin(2.0 * PI * freq * t)
              val overtone = 0.25 * sin(4.0 * PI * freq * t + 0.3)
              chordsWave += (fundamental + overtone) * (0.4 / (noteIdx + 1))
            }
          }

          // 2. Bassline stem (Root sub-harmonic note pulsing on beat)
          var bassWave = 0.0
          if (effBass > 0f) {
            val rootBassFreq = activeNotes.firstOrNull() ?: 110.0f
            val bassEnv = 0.7 + 0.3 * sin(2.0 * PI * (trackBpm / 60.0) * t)
            bassWave = (sin(2.0 * PI * rootBassFreq * t) + 0.35 * sin(PI * rootBassFreq * t)) * bassEnv
          }

          // 3. Vocals / Melody stem (Arpeggiated melodic lead flowing above the chords)
          var vocalMelodyWave = 0.0
          if (effVocals > 0f) {
            val noteStep = ((currentSec * (trackBpm / 30.0)).toInt()) % activeNotes.size
            val leadFreq = (activeNotes.getOrElse(noteStep) { 440f }) * 2.0f
            val vibrato = 1.0 + 0.008 * sin(2.0 * PI * 5.5 * t)
            val leadNote = sin(2.0 * PI * (leadFreq * vibrato) * t)
            val leadHarmonic = 0.3 * sin(4.0 * PI * (leadFreq * vibrato) * t)
            vocalMelodyWave = (leadNote + leadHarmonic) * 0.6
          }

          // 4. Rhythm & Beats stem (Acoustic kick & snare click on tempo)
          var rhythmWave = 0.0
          if (effRhythm > 0f) {
            val beatPhase = (currentSec / beatDurationSec) % 1.0
            val beatNumber = (currentSec / beatDurationSec).toInt() % 4
            // Kick on beats 0 and 2, Snare on beats 1 and 3
            if (beatNumber == 0 || beatNumber == 2) {
              // Deep Kick click & decay
              if (beatPhase < 0.15) {
                val decay = 1.0 - (beatPhase / 0.15)
                val kickFreq = 65.0 * decay + 40.0
                rhythmWave = sin(2.0 * PI * kickFreq * t) * decay * 0.9
              }
            } else {
              // Snare / Hi-hat burst
              if (beatPhase < 0.10) {
                val decay = 1.0 - (beatPhase / 0.10)
                val noise = (random.nextDouble() * 2.0 - 1.0)
                rhythmWave = noise * decay * 0.55
              }
            }
          }

          // Mix stems with limiter
          val composite = (chordsWave * effChords * 0.45 +
              bassWave * effBass * 0.55 +
              vocalMelodyWave * effVocals * 0.50 +
              rhythmWave * effRhythm * 0.40) * 16000.0

          val clamped = composite.coerceIn(-31500.0, 31500.0)
          buffer[i] = clamped.toInt().toShort()
          sumSquares += clamped * clamped
        }

        sampleIndex += buffer.size

        try {
          audioTrack?.write(buffer, 0, buffer.size)
        } catch (e: Exception) {
          Log.e("RealtimeAudioEngine", "AudioTrack write error", e)
        }

        val rms = sqrt(sumSquares / buffer.size) / 32768.0
        val normalizedRms = (rms.toFloat() * 2.8f).coerceIn(0f, 1f)
        _liveRmsAmplitude.value = normalizedRms

        val speedFactor = playbackSpeed
        val currentSec = positionMs / 1000.0
        val bandPulse = sin(currentSec * 8.0 * speedFactor).toFloat()
        val bandPulse2 = sin(currentSec * 12.0 * speedFactor + 1.2).toFloat()
        val bandPulse3 = sin(currentSec * 5.0 * speedFactor + 2.5).toFloat()

        _liveFrequencyBands.value = listOf(
          (normalizedRms * 0.95f + 0.15f * (bandPulse * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.85f + 0.20f * (bandPulse3 * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.75f + 0.25f * (bandPulse2 * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.90f + 0.18f * (bandPulse * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.80f + 0.22f * (bandPulse2 * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.70f + 0.15f * (bandPulse3 * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.60f + 0.12f * (bandPulse * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.50f + 0.10f * (bandPulse2 * 0.5f + 0.5f)).coerceIn(0.08f, 1f)
        )

        delay(16)
      }
    }
  }

  fun pause() {
    if (!isPlayingState) return
    isPlayingState = false
    _isPlaying.value = false
    playbackJob?.cancel()
    playbackJob = null
    try {
      audioTrack?.pause()
      audioTrack?.flush()
    } catch (e: Exception) {
      Log.e("RealtimeAudioEngine", "AudioTrack pause error", e)
    }
    resetLiveMeters()
  }

  fun stop() {
    isPlayingState = false
    _isPlaying.value = false
    playbackJob?.cancel()
    playbackJob = null
    try {
      audioTrack?.stop()
      audioTrack?.flush()
      audioTrack?.release()
    } catch (e: Exception) {
      Log.e("RealtimeAudioEngine", "AudioTrack stop error", e)
    }
    audioTrack = null
    resetLiveMeters()
  }

  fun seekTo(newPositionMs: Long) {
    positionMs = newPositionMs.coerceIn(0L, durationMs)
    _currentPosition.value = positionMs
    if (isPlayingState) {
      try {
        audioTrack?.flush()
      } catch (e: Exception) {
        Log.e("RealtimeAudioEngine", "AudioTrack flush error", e)
      }
    }
  }

  fun setVolume(vol: Float) {
    volumeLevel = vol.coerceIn(0f, 1f)
  }

  fun setMuted(muted: Boolean) {
    isMuted = muted
  }

  fun setPlaybackSpeed(speed: Float) {
    playbackSpeed = speed.coerceIn(0.5f, 2.0f)
  }

  fun setLooping(loop: Boolean) {
    isLooping = loop
  }

  private fun initAudioTrack() {
    if (audioTrack == null) {
      try {
        audioTrack = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(bufferSize)
          .setTransferMode(AudioTrack.MODE_STREAM)
          .build()
      } catch (e: Exception) {
        Log.e("RealtimeAudioEngine", "Error creating AudioTrack", e)
      }
    }
  }

  private fun resetLiveMeters() {
    _liveRmsAmplitude.value = 0f
    _liveFrequencyBands.value = List(8) { 0.05f }
  }
}
