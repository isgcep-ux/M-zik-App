package com.example.audio

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object FfmpegMergeHelper {

  /**
   * Returns the exact FFmpeg command line used for merging vocals and instrumental stems
   * with individual gain control, audio mixing, soft limiter, and 320 kbps MP3 encoding.
   */
  fun getFfmpegCommandLine(
    vocalsGain: Float = 1.0f,
    instrumentalGain: Float = 0.85f,
    vocalsFile: String = "vocals_stem.wav",
    instrumentalFile: String = "instrumental_stem.wav",
    outputFile: String = "master_merged.mp3"
  ): String {
    return """
ffmpeg -y \
  -i "$vocalsFile" \
  -i "$instrumentalFile" \
  -filter_complex "[0:a]volume=${"%.2f".format(vocalsGain)}[v];[1:a]volume=${"%.2f".format(instrumentalGain)}[inst];[v][inst]amix=inputs=2:duration=first:dropout_transition=2,alimiter=limit=0.95[out]" \
  -map "[out]" \
  -c:a libmp3lame \
  -b:a 320k \
  -ar 44100 \
  "$outputFile"
    """.trimIndent()
  }

  /**
   * Kotlin code snippet for executing FFmpeg merge in Android using FFmpegKit
   */
  val ffmpegKitKotlinSnippet: String = """
// ============================================================
// ANDROID FFMPEG MERGE (SES BİRLEŞTİRME) KODU
// Gradle: implementation("com.arthenica:ffmpeg-kit-full:6.0-2")
// ============================================================

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun mergeAudioStems(
    vocalsFile: File,
    instrumentalFile: File,
    outputFile: File,
    vocalsVolume: Float = 1.0f,
    instrumentalVolume: Float = 0.85f,
    onProgress: (String) -> Unit = {}
): Boolean = withContext(Dispatchers.IO) {
    // FFmpeg filtre zinciri:
    // 1. Vokal ve enstrümantal ses seviyelerini ayarla
    // 2. amix filtresiyle 2 kanalı kusursuz birleştir
    // 3. alimiter ile patlamayı (clipping) önle ve 320kbps MP3 olarak kaydet
    val command = arrayOf(
        "-y",
        "-i", vocalsFile.absolutePath,
        "-i", instrumentalFile.absolutePath,
        "-filter_complex",
        "[0:a]volume=${'$'}{vocalsVolume}[v];[1:a]volume=${'$'}{instrumentalVolume}[inst];[v][inst]amix=inputs=2:duration=first:dropout_transition=2,alimiter=limit=0.95[out]",
        "-map", "[out]",
        "-c:a", "libmp3lame",
        "-b:a", "320k",
        "-ar", "44100",
        outputFile.absolutePath
    ).joinToString(" ")

    val session = FFmpegKit.execute(command)
    val returnCode = session.returnCode

    if (ReturnCode.isSuccess(returnCode)) {
        onProgress("Birleştirme tamamlandı: ${'$'}{outputFile.name}")
        true
    } else {
        val failLog = session.failStackTrace ?: session.allLogsAsString
        onProgress("FFmpeg Hatası: ${'$'}failLog")
        false
    }
}
""".trimIndent()

  /**
   * Native Offline Audio Stem Merger (Pure Kotlin PCM export).
   * Blends synthesized stems and writes a standard 44.1kHz 16-bit Stereo WAV file
   * directly to app cache/files with zero external dependencies.
   */
  fun exportSynthesizedTrackToWav(
    context: Context,
    track: AudioTrackInfo,
    vocalsVol: Float = 1.0f,
    instrumentalVol: Float = 0.85f,
    durationSeconds: Int = 30
  ): File {
    val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.cacheDir
    val outputFile = File(outputDir, "${track.title.replace(Regex("[^a-zA-Z0-9_]"), "_")}_merged.wav")

    val sampleRate = 44100
    val channels = 2
    val totalSamples = sampleRate * durationSeconds
    val pcmData = ByteArray(totalSamples * channels * 2)
    val byteBuffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

    val chords = track.chordProgression.ifEmpty {
      listOf(ChordTheory.getChordFrequencies("Dm"))
    }
    val bpm = track.bpm.coerceAtLeast(60)
    val beatDurationSec = 60.0 / bpm
    val chordDurationSec = beatDurationSec * 4.0

    for (sample in 0 until totalSamples) {
      val t = sample.toDouble() / sampleRate
      val chordIdx = ((t / chordDurationSec).toInt()) % chords.size
      val activeNotes = chords[chordIdx]

      // Instrumental chords & bass
      var chordsWave = 0.0
      for ((noteIdx, freq) in activeNotes.withIndex()) {
        val harmonic = sin(2.0 * PI * freq * t) + 0.25 * sin(4.0 * PI * freq * t)
        chordsWave += harmonic * (0.35 / (noteIdx + 1))
      }
      val bassFreq = activeNotes.firstOrNull() ?: 146.83f
      val bassWave = sin(2.0 * PI * bassFreq * t) * 0.55

      // Melody / Vocals
      val noteStep = ((t * (bpm / 30.0)).toInt()) % activeNotes.size
      val leadFreq = (activeNotes.getOrElse(noteStep) { 440f }) * 2.0f
      val leadWave = sin(2.0 * PI * leadFreq * t) * 0.60

      val mixedLeft = (chordsWave * 0.5 + bassWave * 0.5) * instrumentalVol + (leadWave * 0.85) * vocalsVol
      val mixedRight = (chordsWave * 0.55 + bassWave * 0.45) * instrumentalVol + (leadWave * 0.80) * vocalsVol

      val clampedL = (mixedLeft * 14000.0).coerceIn(-32000.0, 32000.0).toInt().toShort()
      val clampedR = (mixedRight * 14000.0).coerceIn(-32000.0, 32000.0).toInt().toShort()

      byteBuffer.putShort(clampedL)
      byteBuffer.putShort(clampedR)
    }

    // Write WAV header and PCM data
    FileOutputStream(outputFile).use { fos ->
      writeWavHeader(fos, totalSamples * channels * 2, sampleRate, channels)
      fos.write(pcmData)
    }

    return outputFile
  }

  private fun writeWavHeader(out: FileOutputStream, pcmSize: Int, sampleRate: Int, channels: Int) {
    val totalDataLen = pcmSize + 36
    val byteRate = sampleRate * channels * 2

    val header = ByteArray(44)
    // RIFF/WAVE header
    header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
    header[4] = (totalDataLen and 0xff).toByte()
    header[5] = ((totalDataLen shr 8) and 0xff).toByte()
    header[6] = ((totalDataLen shr 16) and 0xff).toByte()
    header[7] = ((totalDataLen shr 24) and 0xff).toByte()
    header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
    // 'fmt ' chunk
    header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
    header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // 16 for PCM
    header[20] = 1; header[21] = 0 // Audio format 1 = PCM
    header[22] = channels.toByte(); header[23] = 0
    header[24] = (sampleRate and 0xff).toByte()
    header[25] = ((sampleRate shr 8) and 0xff).toByte()
    header[26] = ((sampleRate shr 16) and 0xff).toByte()
    header[27] = ((sampleRate shr 24) and 0xff).toByte()
    header[28] = (byteRate and 0xff).toByte()
    header[29] = ((byteRate shr 8) and 0xff).toByte()
    header[30] = ((byteRate shr 16) and 0xff).toByte()
    header[31] = ((byteRate shr 24) and 0xff).toByte()
    header[32] = (channels * 2).toByte(); header[33] = 0 // block align
    header[34] = 16; header[35] = 0 // bits per sample
    // 'data' chunk
    header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
    header[40] = (pcmSize and 0xff).toByte()
    header[41] = ((pcmSize shr 8) and 0xff).toByte()
    header[42] = ((pcmSize shr 16) and 0xff).toByte()
    header[43] = ((pcmSize shr 24) and 0xff).toByte()

    out.write(header, 0, 44)
  }
}
