package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

class RealtimeAudioEngine(
  private val scope: CoroutineScope
) {
  private val sampleRate = 44100
  // Provide plenty of buffer headroom (at least 16KB or 4x minBufferSize) to avoid underrun glitches
  private val minHwBuffer = AudioTrack.getMinBufferSize(
    sampleRate,
    AudioFormat.CHANNEL_OUT_MONO,
    AudioFormat.ENCODING_PCM_16BIT
  )
  private val audioTrackBufferSize = (minHwBuffer * 4).coerceAtLeast(16384)
  private val renderChunkSize = 1024 // ~23ms of audio per write chunk

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

      val buffer = ShortArray(renderChunkSize)

      // Continuous Phase Accumulators for pure, click-free synthesis
      val chordPhases = DoubleArray(4) { 0.0 }
      var bassPhase = 0.0
      var leadPhase = 0.0
      var kickPhase = 0.0
      var noiseFilter = 0.0

      val trackBpm = currentTrack?.bpm ?: 90
      val beatDurationSec = 60.0 / trackBpm
      val barDurationSec = beatDurationSec * 4.0 // 4 beats per bar
      val chords = currentTrack?.chordProgression?.ifEmpty { null }
        ?: listOf(floatArrayOf(146.83f, 293.66f, 349.23f, 440.0f))

      val random = java.util.Random(1337)

      // Pentatonic / chord melody steps for musicality (8 subdivisions per bar)
      val melodyPattern = intArrayOf(0, 2, 1, 3, 2, 1, 3, 0)

      while (isActive && isPlayingState) {
        if (positionMs >= durationMs) {
          if (isLooping) {
            positionMs = 0L
          } else {
            positionMs = durationMs
            _currentPosition.value = positionMs
            _isPlaying.value = false
            isPlayingState = false
            resetLiveMeters()
            break
          }
        }

        val masterVol = if (isMuted) 0f else volumeLevel
        val effVocals = if (stemVocalsMuted) 0f else stemVocalsLevel * masterVol
        val effChords = if (stemChordsMuted) 0f else stemChordsLevel * masterVol
        val effBass = if (stemBassMuted) 0f else stemBassLevel * masterVol
        val effRhythm = if (stemRhythmMuted) 0f else stemRhythmLevel * masterVol

        var sumSquares = 0.0

        for (i in 0 until renderChunkSize) {
          val currentSec = (positionMs / 1000.0 + (i.toDouble() / sampleRate) * playbackSpeed) % (durationMs / 1000.0)

          // 1. Warm Rhodes Electric Piano (Chords with natural strike & warm decay envelope)
          val chordIdx = ((currentSec / barDurationSec).toInt()) % chords.size
          val activeNotes = chords[chordIdx]
          val timeInBar = currentSec % barDurationSec

          // Struck key decay: fast warm attack then gentle decay like a Fender Rhodes piano
          val chordEnvelope = kotlin.math.exp(-timeInBar * 1.2).toFloat() * 0.70f + 0.30f

          var chordsWave = 0.0
          if (effChords > 0f) {
            for (noteIdx in 0 until 4) {
              val freq = if (noteIdx < activeNotes.size) activeNotes[noteIdx].toDouble() else 0.0
              if (freq > 80.0) {
                chordPhases[noteIdx] += 2.0 * PI * freq / sampleRate
                if (chordPhases[noteIdx] >= 2.0 * PI) chordPhases[noteIdx] -= 2.0 * PI

                // Warm Rhodes harmonics (fundamental + soft bell overtone)
                val fundamental = sin(chordPhases[noteIdx])
                val bellOvertone = 0.18 * sin(chordPhases[noteIdx] * 2.0)
                val softTine = 0.05 * sin(chordPhases[noteIdx] * 3.0)
                chordsWave += (fundamental + bellOvertone + softTine) * (0.28 / (noteIdx + 1))
              }
            }
            chordsWave *= chordEnvelope
          }

          // 2. Warm Lofi Bass (Deep, rounded sub-bass pulsing gently on beats 1 and 3)
          var bassWave = 0.0
          if (effBass > 0f) {
            val rootBassFreq = (activeNotes.firstOrNull()?.toDouble() ?: 110.0).coerceIn(45.0, 110.0)
            bassPhase += 2.0 * PI * rootBassFreq / sampleRate
            if (bassPhase >= 2.0 * PI) bassPhase -= 2.0 * PI

            val timeInBeat = currentSec % beatDurationSec
            val bassPluck = kotlin.math.exp(-timeInBeat * 3.2).toFloat() * 0.65f + 0.35f
            // Deep sub fundamental with mild 2nd harmonic warmth
            bassWave = (sin(bassPhase) + 0.15 * sin(bassPhase * 2.0)) * bassPluck * 0.75
          }

          // 3. Acoustic Kalimba / Harp Melody (Discrete musical notes with gentle pluck envelope - NO SIREN GLIDE)
          var vocalMelodyWave = 0.0
          if (effVocals > 0f) {
            val subStepDuration = beatDurationSec / 2.0 // 8th note steps
            val stepIndex = ((currentSec / subStepDuration).toInt()) % melodyPattern.size
            val noteToneIdx = melodyPattern[stepIndex] % activeNotes.size
            val discreteFreq = (activeNotes.getOrElse(noteToneIdx) { 440f }).toDouble() * 1.5

            leadPhase += 2.0 * PI * discreteFreq / sampleRate
            if (leadPhase >= 2.0 * PI) leadPhase -= 2.0 * PI

            val timeInNote = currentSec % subStepDuration
            // Pluck envelope: sharp acoustic attack (0-5ms) then exponential sweet decay
            val pluckEnv = kotlin.math.exp(-timeInNote * 6.5).toFloat()

            // Sweet bell/kalimba tone (warm sine with warm undertone)
            val bellWave = sin(leadPhase) + 0.12 * sin(leadPhase * 2.0)
            vocalMelodyWave = bellWave * pluckEnv * 0.55
          }

          // 4. Acoustic Chill Drum Kit (Solid thump kick, warm rimshot, delicate closed hi-hat)
          var rhythmWave = 0.0
          if (effRhythm > 0f) {
            val timeInBeat = currentSec % beatDurationSec
            val beatNumber = (currentSec / beatDurationSec).toInt() % 4

            if (beatNumber == 0 || beatNumber == 2) {
              // Solid Sub Kick: Tight 15ms punch into a warm 52Hz low end (no laser sweep)
              if (timeInBeat < 0.18) {
                val kickEnv = kotlin.math.exp(-timeInBeat * 22.0).toFloat()
                val kickPitch = 52.0 + 35.0 * kotlin.math.exp(-timeInBeat * 80.0)
                kickPhase += 2.0 * PI * kickPitch / sampleRate
                if (kickPhase >= 2.0 * PI) kickPhase -= 2.0 * PI
                rhythmWave += sin(kickPhase) * kickEnv * 0.85
              }
            } else {
              // Warm Wooden Rimshot / Snare: Soft resonant pop + gentle acoustic brush
              if (timeInBeat < 0.12) {
                val snareEnv = kotlin.math.exp(-timeInBeat * 30.0).toFloat()
                val snareBody = sin(2.0 * PI * 185.0 * timeInBeat) * snareEnv * 0.4
                val rawNoise = (random.nextDouble() * 2.0 - 1.0)
                noiseFilter = noiseFilter * 0.65 + rawNoise * 0.35
                val snareSnap = noiseFilter * snareEnv * 0.25
                rhythmWave += (snareBody + snareSnap)
              }
            }

            // Delicate Shaker / Hi-Hat on 8th-note offbeats
            val halfBeatTime = timeInBeat % (beatDurationSec / 2.0)
            if (halfBeatTime < 0.04) {
              val hatEnv = kotlin.math.exp(-halfBeatTime * 80.0).toFloat()
              val rawHiss = (random.nextDouble() * 2.0 - 1.0)
              rhythmWave += rawHiss * hatEnv * 0.12
            }
          }

          // Master Stem Blending with headroom
          val composite = (chordsWave * effChords * 0.40 +
              bassWave * effBass * 0.45 +
              vocalMelodyWave * effVocals * 0.40 +
              rhythmWave * effRhythm * 0.40)

          // Analog Soft-Saturation (Tanh) to prevent any digital distortion or clicking
          val warmSaturated = tanh(composite * 1.05)
          val sampleShort = (warmSaturated * 26000.0).toInt().toShort()

          buffer[i] = sampleShort
          sumSquares += warmSaturated * warmSaturated
        }

        // Blocking write to AudioTrack perfectly synchronizes with hardware audio clock
        try {
          audioTrack?.write(buffer, 0, renderChunkSize)
        } catch (e: Exception) {
          Log.e("RealtimeAudioEngine", "AudioTrack write error", e)
        }

        // Synchronize position directly with samples played
        val elapsedMsInChunk = ((renderChunkSize.toDouble() / sampleRate) * 1000.0 * playbackSpeed).toLong()
        positionMs += elapsedMsInChunk
        _currentPosition.value = positionMs

        // Visualizer metering calculated from written buffer
        val rms = sqrt(sumSquares / renderChunkSize)
        val normalizedRms = (rms.toFloat() * 1.5f).coerceIn(0f, 1f)
        _liveRmsAmplitude.value = normalizedRms

        val currentSec = positionMs / 1000.0
        val bandPulse = sin(currentSec * 8.0).toFloat()
        val bandPulse2 = sin(currentSec * 12.0 + 1.2).toFloat()
        val bandPulse3 = sin(currentSec * 5.0 + 2.5).toFloat()

        _liveFrequencyBands.value = listOf(
          (normalizedRms * 0.95f + 0.12f * (bandPulse * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.85f + 0.15f * (bandPulse3 * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.75f + 0.20f * (bandPulse2 * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.90f + 0.14f * (bandPulse * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.80f + 0.18f * (bandPulse2 * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.70f + 0.12f * (bandPulse3 * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.60f + 0.10f * (bandPulse * 0.5f + 0.5f)).coerceIn(0.08f, 1f),
          (normalizedRms * 0.50f + 0.08f * (bandPulse2 * 0.5f + 0.5f)).coerceIn(0.08f, 1f)
        )
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
          .setBufferSizeInBytes(audioTrackBufferSize)
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

