package com.example.audio

import kotlin.math.pow
import kotlin.math.sin

enum class StemType(val label: String, val badgeColor: Long) {
  MASTER("Master Mix", 0xFF06B6D4),
  VOCALS("Vocals Stem", 0xFFEC4899),
  BACKING("Backing Stem", 0xFF8B5CF6),
  INSTRUMENTAL("Acoustic Melody", 0xFF10B981),
  AI_ORIGINAL("AI Beste", 0xFFA855F7)
}

data class LyricSection(
  val tag: String,
  val lyrics: String
)

data class AudioTrackInfo(
  val id: String,
  val title: String,
  val artist: String,
  val stemType: StemType = StemType.AI_ORIGINAL,
  val durationMs: Long = 180000L,
  val bpm: Int = 92,
  val keySignature: String = "D Minor",
  val genre: String = "Pop",
  val mood: String = "Duygusal",
  val formatSpec: String = "320 kbps MP3 • 44.1 kHz",
  val waveformData: List<Float> = emptyList(),
  val chordProgression: List<FloatArray> = emptyList(),
  val chordNames: List<String> = listOf("Dm", "C", "Bb", "A"),
  val lyricsSections: List<LyricSection> = emptyList(),
  val aiPrompt: String = "",
  val isAiGenerated: Boolean = false
) {
  val fullLyricsText: String
    get() = if (lyricsSections.isNotEmpty()) {
      lyricsSections.joinToString("\n\n") { "[${it.tag}]\n${it.lyrics}" }
    } else {
      "Bu parça için söz bilgisi bulunamadı."
    }
}

object ChordTheory {
  // Base frequencies in 4th octave (A4 = 440 Hz standard)
  private val noteOffsets = mapOf(
    "C" to -9,
    "C#" to -8, "DB" to -8,
    "D" to -7,
    "D#" to -6, "EB" to -6,
    "E" to -5,
    "F" to -4,
    "F#" to -3, "GB" to -3,
    "G" to -2,
    "G#" to -1, "AB" to -1,
    "A" to 0,
    "A#" to 1, "BB" to 1,
    "B" to 2
  )

  fun getChordFrequencies(chordName: String): FloatArray {
    val clean = chordName.trim().uppercase()
    var root = ""
    var isMinor = false
    var isSeventh = false

    // Identify root note
    if (clean.length >= 2 && (clean[1] == '#' || clean[1] == 'B')) {
      root = clean.substring(0, 2)
      val remainder = clean.substring(2)
      isMinor = remainder.contains("M") && !remainder.contains("MAJ")
      isSeventh = remainder.contains("7")
    } else if (clean.isNotEmpty()) {
      root = clean.substring(0, 1)
      val remainder = clean.substring(1)
      isMinor = remainder.contains("M") && !remainder.contains("MAJ")
      isSeventh = remainder.contains("7")
    }

    val semitoneOffset = noteOffsets[root] ?: 0
    val rootFreq = (440.0 * 2.0.pow(semitoneOffset / 12.0)).toFloat()
    val thirdInterval = if (isMinor) 3 else 4
    val fifthInterval = 7
    val thirdFreq = (rootFreq * 2.0.pow(thirdInterval / 12.0)).toFloat()
    val fifthFreq = (rootFreq * 2.0.pow(fifthInterval / 12.0)).toFloat()
    val bassFreq = rootFreq / 2f

    return if (isSeventh) {
      val seventhInterval = 10
      val seventhFreq = (rootFreq * 2.0.pow(seventhInterval / 12.0)).toFloat()
      floatArrayOf(bassFreq, rootFreq, thirdFreq, fifthFreq, seventhFreq)
    } else {
      floatArrayOf(bassFreq, rootFreq, thirdFreq, fifthFreq)
    }
  }

  fun generateWaveformForEnergy(barCount: Int = 90, energy: Float = 0.8f, seed: Long = 42L): List<Float> {
    val random = java.util.Random(seed)
    return List(barCount) { i ->
      val progress = i.toFloat() / barCount
      val envelope = when {
        progress < 0.12f -> 0.20f + (progress / 0.12f) * 0.40f
        progress in 0.30f..0.55f -> 0.75f + (sin(progress * 16.0).toFloat() * 0.20f)
        progress in 0.65f..0.85f -> 0.85f + (sin(progress * 18.0).toFloat() * 0.15f)
        progress > 0.88f -> 0.5f * (1.0f - (progress - 0.88f) / 0.12f) + 0.15f
        else -> 0.50f
      }
      val noise = (random.nextFloat() * 0.30f) - 0.15f
      (envelope * energy + noise).coerceIn(0.10f, 0.98f)
    }
  }
}

object TrackCatalog {
  fun getSampleTracks(): List<AudioTrackInfo> {
    return listOf(
      AudioTrackInfo(
        id = "ai_ask_01",
        title = "Aşkınla Beni (AI Master)",
        artist = "AI Music Composer • Kits + ElevenLabs",
        stemType = StemType.MASTER,
        durationMs = 184000L,
        bpm = 92,
        keySignature = "D Minor",
        genre = "Türk Pop / Slow",
        mood = "Duygusal & Tutkulu",
        formatSpec = "320 kbps MP3 • 44.1 kHz • Stereo",
        waveformData = ChordTheory.generateWaveformForEnergy(energy = 0.92f, seed = 42L),
        chordNames = listOf("Dm", "C", "Bb", "A"),
        chordProgression = listOf(
          ChordTheory.getChordFrequencies("Dm"),
          ChordTheory.getChordFrequencies("C"),
          ChordTheory.getChordFrequencies("Bb"),
          ChordTheory.getChordFrequencies("A")
        ),
        lyricsSections = listOf(
          LyricSection("GİRİŞ", "(Piyano akorları ve derin bas ritmi)"),
          LyricSection("KITA 1", "Müzik sustu gecenin tam ortasında\nKelimeler asılı kaldı dudaklarımda\nBir bakışın yetti bin yıllık yangına\nAşkınla beni sar yeniden."),
          LyricSection("NAKARAT", "Aşkınla beni yak, kül et istersen\nRazıyım ben seninle geçen her güze\nYarım kalan masalımız bitmesin böyle\nSensiz bu dünya koca bir virane."),
          LyricSection("KITA 2", "Gözlerinde saklı kalan son umutlar\nRüzgarla savruldu eski hatıralar\nTut ellerimi bırakma sabaha kadar\nAşkınla beni uyandır yarınlara."),
          LyricSection("ÇIKIŞ", "Sensiz sonsuza dek...\nYalnızlık bitti artık aşkınla beni sar.")
        ),
        aiPrompt = "Duygusal Türk pop şarkısı, hüzünlü piyano, yaylılar ve derin aşk sözleri",
        isAiGenerated = true
      ),
      AudioTrackInfo(
        id = "ai_masal_02",
        title = "O Güzel Masalımız",
        artist = "AI Ballad Composer",
        stemType = StemType.INSTRUMENTAL,
        durationMs = 152000L,
        bpm = 84,
        keySignature = "A Minor",
        genre = "Akustik Ballad",
        mood = "Hüzünlü & Sakin",
        formatSpec = "320 kbps MP3 • 44.1 kHz",
        waveformData = ChordTheory.generateWaveformForEnergy(energy = 0.88f, seed = 101L),
        chordNames = listOf("Am", "F", "C", "G"),
        chordProgression = listOf(
          ChordTheory.getChordFrequencies("Am"),
          ChordTheory.getChordFrequencies("F"),
          ChordTheory.getChordFrequencies("C"),
          ChordTheory.getChordFrequencies("G")
        ),
        lyricsSections = listOf(
          LyricSection("GİRİŞ", "(Akustik gitar arpejleri)"),
          LyricSection("KITA 1", "Müzik sustu...\nBitti o son dansımız\nEllerim bomboş kaldı salonda\nYarım kaldı o güzel masalımız."),
          LyricSection("NAKARAT", "Yarım kaldı o güzel masalımız\nKimse bilmez neydi bizim sevdamız\nGözyaşlarımla ıslandı notalar\nSuskun artık bütün şarkılar."),
          LyricSection("KÖPRÜ", "Zaman geri aksa, melodin çalsa\nEllerim ellerinde kaybolsa..."),
          LyricSection("ÇIKIŞ", "(Gitar solo ve sönen reverbler)")
        ),
        aiPrompt = "Akustik gitar ve piyano ile hüzünlü slow aşk şarkısı",
        isAiGenerated = true
      ),
      AudioTrackInfo(
        id = "ai_synth_03",
        title = "Neon Gece Sürüşü",
        artist = "AI Cyberwave Engine",
        stemType = StemType.AI_ORIGINAL,
        durationMs = 168000L,
        bpm = 118,
        keySignature = "E Minor",
        genre = "Synthwave / Cyberpunk",
        mood = "Enerjik & Fütüristik",
        formatSpec = "320 kbps MP3 • 44.1 kHz • Hi-Fi",
        waveformData = ChordTheory.generateWaveformForEnergy(energy = 0.95f, seed = 202L),
        chordNames = listOf("Em", "C", "G", "D"),
        chordProgression = listOf(
          ChordTheory.getChordFrequencies("Em"),
          ChordTheory.getChordFrequencies("C"),
          ChordTheory.getChordFrequencies("G"),
          ChordTheory.getChordFrequencies("D")
        ),
        lyricsSections = listOf(
          LyricSection("GİRİŞ", "(Analog synth arpeggiator ve 808 bas vuruşu)"),
          LyricSection("KITA 1", "Işıklar akar camdan geriye\nŞehir uyur karanlık denizde\nHız göstergesi dönerken geceye\nYapay zeka ritmi çalar kalbimde."),
          LyricSection("NAKARAT", "Neon ışıklar altında son sürat\nGeride kaldı bütün dertler hayat\nGece bizim, bu frekans bizim\nSynthesizer sonsuzluğa uzanır sesim!"),
          LyricSection("ÇIKIŞ", "(Siren filtreleri ve synthesizer fade-out)")
        ),
        aiPrompt = "80'ler synthwave, neon gece sürüşü, hızlı 808 bas ve arpejli synthesizer",
        isAiGenerated = true
      ),
      AudioTrackInfo(
        id = "ai_lofi_04",
        title = "Yağmurlu Kahve",
        artist = "AI Lo-Fi Lab",
        stemType = StemType.BACKING,
        durationMs = 140000L,
        bpm = 76,
        keySignature = "C Major",
        genre = "Lo-Fi Chillhop",
        mood = "Dingin & Rahatlatıcı",
        formatSpec = "320 kbps MP3 • Vinil Crackle Effect",
        waveformData = ChordTheory.generateWaveformForEnergy(energy = 0.72f, seed = 303L),
        chordNames = listOf("Cmaj7", "Am7", "Dm7", "G7"),
        chordProgression = listOf(
          ChordTheory.getChordFrequencies("C"),
          ChordTheory.getChordFrequencies("Am"),
          ChordTheory.getChordFrequencies("Dm"),
          ChordTheory.getChordFrequencies("G")
        ),
        lyricsSections = listOf(
          LyricSection("ORTAM", "(Pencereye vuran yağmur damlaları ve vinil cızırtısı)"),
          LyricSection("KITA 1", "Pencere kenarı, sıcak bir fincan\nZaman yavaşlar yağmur başladığı an\nKitap sayfaları arasında kayıp\nSakin bir nota ruhumu sarıp."),
          LyricSection("NAKARAT", "Damlalar ritim tutar cama\nUzaklaşır telaşlar bu akşama\nChill melodiler akar odaya\nDinginlik hediye bu dünyaya.")
        ),
        aiPrompt = "Lo-fi chill beat, yağmur sesi, elektrikli piyano caz akorları",
        isAiGenerated = true
      )
    )
  }
}
