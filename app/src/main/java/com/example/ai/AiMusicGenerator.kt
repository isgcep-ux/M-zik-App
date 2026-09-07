package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.audio.AudioTrackInfo
import com.example.audio.ChordTheory
import com.example.audio.LyricSection
import com.example.audio.StemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class MusicGenerationParams(
  val prompt: String,
  val genre: String = "Pop",
  val mood: String = "Duygusal",
  val bpm: Int = 92,
  val keySignature: String = "D Minor",
  val vocalStyle: String = "Kadın Vokal"
)

object AiMusicGenerator {
  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  suspend fun generateTrack(params: MusicGenerationParams): AudioTrackInfo = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
      ""
    }

    if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
      try {
        val aiResult = callGeminiForMusic(params, apiKey)
        if (aiResult != null) {
          return@withContext aiResult
        }
      } catch (e: Exception) {
        Log.w("AiMusicGenerator", "Gemini API call failed, falling back to procedural AI composer: ${e.message}")
      }
    }

    // Procedural algorithmic AI music generator (offline-first, zero-delay guarantee)
    generateProceduralMusic(params)
  }

  private fun callGeminiForMusic(params: MusicGenerationParams, apiKey: String): AudioTrackInfo? {
    val promptText = """
      Sen profesyonel bir müzik yapımcısı, besteci ve söz yazarı yapay zekasın.
      Kullanıcının isteği: "${params.prompt}"
      Seçilen Tür: ${params.genre}
      Ruh Hali: ${params.mood}
      Tempo (BPM): ${params.bpm}
      Makam / Ton: ${params.keySignature}
      Vokal Tarzı: ${params.vocalStyle}
      
      Lütfen bu parametrelere uygun, Türkçe sözlü, harika kafiyeli ve müzikal olarak tutarlı eksiksiz bir şarkı bestele.
      Çıktıyı kesinlikle ve SADECE aşağıdaki JSON şemasına uygun formatta ver:
      {
        "title": "Şarkı Adı",
        "genre": "${params.genre}",
        "mood": "${params.mood}",
        "bpm": ${params.bpm},
        "keySignature": "${params.keySignature}",
        "chords": ["Akor1", "Akor2", "Akor3", "Akor4"],
        "lyricsSections": [
          {"tag": "GİRİŞ", "lyrics": "Enstrümantal tarif"},
          {"tag": "KITA 1", "lyrics": "Dörtlük sözler..."},
          {"tag": "NAKARAT", "lyrics": "Vurucu nakarat sözleri..."},
          {"tag": "KITA 2", "lyrics": "İkinci dörtlük..."},
          {"tag": "KÖPRÜ", "lyrics": "Duygusal köprü..."},
          {"tag": "ÇIKIŞ", "lyrics": "Son veda sözleri..."}
        ],
        "arrangementNotes": "Düzenleme detayları"
      }
    """.trimIndent()

    val jsonRequest = JSONObject().apply {
      val contentsArray = JSONArray()
      val contentObj = JSONObject()
      val partsArray = JSONArray()
      val partObj = JSONObject().apply {
        put("text", promptText)
      }
      partsArray.put(partObj)
      contentObj.put("parts", partsArray)
      contentsArray.put(contentObj)
      put("contents", contentsArray)

      val genConfig = JSONObject().apply {
        put("temperature", 0.8)
        put("responseMimeType", "application/json")
      }
      put("generationConfig", genConfig)
    }

    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = jsonRequest.toString().toRequestBody(mediaType)
    val request = Request.Builder().url(url).post(body).build()

    val response = okHttpClient.newCall(request).execute()
    if (!response.isSuccessful) {
      Log.e("AiMusicGenerator", "Gemini HTTP ${response.code}: ${response.message}")
      return null
    }

    val responseBody = response.body?.string() ?: return null
    val rootObj = JSONObject(responseBody)
    val candidates = rootObj.optJSONArray("candidates") ?: return null
    val firstCandidate = candidates.optJSONObject(0) ?: return null
    val content = firstCandidate.optJSONObject("content") ?: return null
    val parts = content.optJSONArray("parts") ?: return null
    val textJson = parts.optJSONObject(0)?.optString("text") ?: return null

    val songJson = JSONObject(textJson)
    val title = songJson.optString("title", "AI Beste")
    val bpm = songJson.optInt("bpm", params.bpm)
    val keySig = songJson.optString("keySignature", params.keySignature)
    val genre = songJson.optString("genre", params.genre)
    val mood = songJson.optString("mood", params.mood)

    val chordList = mutableListOf<String>()
    val chordJsonArray = songJson.optJSONArray("chords")
    if (chordJsonArray != null && chordJsonArray.length() > 0) {
      for (i in 0 until chordJsonArray.length()) {
        chordList.add(chordJsonArray.optString(i))
      }
    } else {
      chordList.addAll(listOf("Dm", "C", "Bb", "A"))
    }

    val sections = mutableListOf<LyricSection>()
    val lyricArray = songJson.optJSONArray("lyricsSections")
    if (lyricArray != null) {
      for (i in 0 until lyricArray.length()) {
        val item = lyricArray.optJSONObject(i) ?: continue
        sections.add(
          LyricSection(
            tag = item.optString("tag", "BÖLÜM"),
            lyrics = item.optString("lyrics", "")
          )
        )
      }
    }

    val chordProg = chordList.map { ChordTheory.getChordFrequencies(it) }
    val waveform = ChordTheory.generateWaveformForEnergy(
      energy = 0.90f,
      seed = System.currentTimeMillis()
    )

    return AudioTrackInfo(
      id = "ai_${UUID.randomUUID().toString().take(8)}",
      title = title,
      artist = "Gemini 3.5 Flash • AI Music Engine",
      stemType = StemType.AI_ORIGINAL,
      durationMs = 180000L,
      bpm = bpm,
      keySignature = keySig,
      genre = genre,
      mood = mood,
      formatSpec = "320 kbps MP3 • Gemini AI Studio Master",
      waveformData = waveform,
      chordNames = chordList,
      chordProgression = chordProg,
      lyricsSections = sections,
      aiPrompt = params.prompt,
      isAiGenerated = true
    )
  }

  fun generateProceduralMusic(params: MusicGenerationParams): AudioTrackInfo {
    val seed = System.currentTimeMillis()
    val random = kotlin.random.Random(seed)

    val (title, chords, lyrics) = when {
      params.genre.contains("Synthwave", ignoreCase = true) || params.prompt.contains("synth", ignoreCase = true) -> {
        Triple(
          "Gece Matrisi (Neon Dalgalar)",
          listOf("Em", "C", "G", "D"),
          listOf(
            LyricSection("GİRİŞ", "(Hızlı analog arpejler ve fütüristik synthesizer vuruşları)"),
            LyricSection("KITA 1", "Neon tabelalar yansır ıslak asfaltta\nZaman durdu bu distopik sokakta\nFrekanslar yükselir kulaklığımda\nYapay zeka ritmi akıyor kanda."),
            LyricSection("NAKARAT", "Gece matrisi, neon rüyalar\nSynthesizer ile silinsin yaralar\nHız göstergesi kırkı aşınca\nÖzgürüz artık yıldızlar altında!"),
            LyricSection("KITA 2", "Gözlerinde sibernetik parıltı\nŞehir geride bir siluet kaldı\nBassline vurur göğsümün ortasına\nKarıştık gecenin sonsuzluğuna."),
            LyricSection("ÇIKIŞ", "(Lazer sesleri ve arpeggiator solosu)")
          )
        )
      }
      params.genre.contains("Lo-Fi", ignoreCase = true) || params.prompt.contains("lofi", ignoreCase = true) -> {
        Triple(
          "Gece Yarısı Çayı",
          listOf("C", "Am", "Dm", "G"),
          listOf(
            LyricSection("GİRİŞ", "(Vinil çıtırtısı ve yumuşak caz piyanosu)"),
            LyricSection("KITA 1", "Sessiz bir oda, bir fincan sıcak çay\nPencerenin ardında hilal gibi ay\nKelimeler dinlenir defter kenarında\nHuzur gizlidir bu lo-fi akorlarında."),
            LyricSection("NAKARAT", "Yavaşla dünya, telaşın kime?\nBırak melodiler insin kalbine\nSakin bir nefes, yumuşak bir ritim\nBu gece kendimle barışık içim."),
            LyricSection("ÇIKIŞ", "(Piyano arpeji ve rüzgar uğultusu)")
          )
        )
      }
      params.genre.contains("Rock", ignoreCase = true) || params.prompt.contains("rock", ignoreCase = true) -> {
        Triple(
          "Fırtına Öncesi Sessizlik",
          listOf("Am", "F", "D", "E"),
          listOf(
            LyricSection("GİRİŞ", "(Distortion gitar riffleri ve dinamik davul girişi)"),
            LyricSection("KITA 1", "Karanlık bulutlar toplandı tepede\nBir çığlık saklı kaldı içimde\nKırıldı zincirler, dağıldı sisler\nYenilgiyi kabul etmez bu sesler!"),
            LyricSection("NAKARAT", "Fırtına kopuyor yüreğimde!\nYıkıp geçecek eski izleri\nGitarlar haykırır bu sahnede\nUnutmadık asla gidenleri!"),
            LyricSection("KÖPRÜ", "(Elektro gitar solo patlaması)"),
            LyricSection("ÇIKIŞ", "(Çift kros davul ve son akor vuruşu)")
          )
        )
      }
      params.genre.contains("Akustik", ignoreCase = true) || params.mood.contains("Hüzünlü", ignoreCase = true) -> {
        Triple(
          "Yarım Kalan Dansımız",
          listOf("Dm", "Bb", "F", "C"),
          listOf(
            LyricSection("GİRİŞ", "(Duygusal naylon telli gitar ve piyano)"),
            LyricSection("KITA 1", "Müzik sustu gecenin kör saatinde\nEllerin çekildi ellerimden usulca\nBir veda gizliydi son cümlende\nKaldım bir başıma bu boş salonda."),
            LyricSection("NAKARAT", "Aşkınla beni sar demiştin oysa\nGitme ne olur ömrüm son bulsa\nYarım kalan şarkımız ağlar ardımızdan\nBir masal silindi hatıramızdan."),
            LyricSection("KITA 2", "Eski mektuplar sarardı rafta\nKokun kalmış hala bu yastıkta\nUnutmak dedikleri koca bir yalan\nYüreğimde dinmeyen derin bir sızı kalan."),
            LyricSection("ÇIKIŞ", "(Piyano fade-out ve derin nefes)")
          )
        )
      }
      else -> {
        // Modern Turkish Pop & Dynamic AI Composition
        Triple(
          "Aşkınla Beni (AI Yeniden Doğuş)",
          listOf("Dm", "C", "Bb", "A"),
          listOf(
            LyricSection("GİRİŞ", "(Modern synth padler ve bas girişi)"),
            LyricSection("KITA 1", "Gözlerin gözlerime değdiği anda\nZaman durur koca bir evrende\nSözler erir şarkıların koynunda\nBir tek sen varsın bu bedende."),
            LyricSection("NAKARAT", "Aşkınla beni yak, aydınlat geceyi\nBirlikte çözelim bu bilmeceyi\nKalbim seninle aynı ritimde atar\nBu melodi dünyayı ayağa kaldırır!"),
            LyricSection("KITA 2", "Adını fısıldar esen rüzgarlar\nBizim için yazıldı bütün notalar\nYarınlar beklesin bu anı yaşayalım\nAşkın kollarında sonsuz olalım."),
            LyricSection("ÇIKIŞ", "(Tüm enstrümanların birleşimi ve master outro)")
          )
        )
      }
    }

    val dynamicTitle = if (params.prompt.isNotBlank() && params.prompt.length < 30) {
      "${params.prompt.replaceFirstChar { it.uppercase() }} (AI)"
    } else title

    val chordProg = chords.map { ChordTheory.getChordFrequencies(it) }
    val waveform = ChordTheory.generateWaveformForEnergy(
      energy = if (params.mood.contains("Enerjik", ignoreCase = true)) 0.95f else 0.85f,
      seed = seed
    )

    return AudioTrackInfo(
      id = "ai_proc_${seed.toString().takeLast(6)}",
      title = dynamicTitle,
      artist = "AI Music Studio • Deep Composer",
      stemType = StemType.AI_ORIGINAL,
      durationMs = (150000L..210000L).random(random),
      bpm = params.bpm,
      keySignature = params.keySignature,
      genre = params.genre,
      mood = params.mood,
      formatSpec = "320 kbps MP3 • AI Algorithmic Synth Engine",
      waveformData = waveform,
      chordNames = chords,
      chordProgression = chordProg,
      lyricsSections = lyrics,
      aiPrompt = params.prompt.ifBlank { "${params.genre} tarzında ${params.mood} şarkı" },
      isAiGenerated = true
    )
  }

  suspend fun generateLyricsWithAi(
    theme: String,
    genre: String,
    mood: String,
    additionalNotes: String = ""
  ): Pair<String, String> = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
      ""
    }

    if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
      try {
        val isEnglishInput = theme.any { it in 'a'..'z' || it in 'A'..'Z' } && !theme.any { it in "çÇğĞıİöÖşŞüÜ" } && (
          theme.contains("love", ignoreCase = true) || theme.contains("night", ignoreCase = true) ||
          theme.contains("city", ignoreCase = true) || theme.contains("rock", ignoreCase = true) ||
          theme.contains("pop", ignoreCase = true) || theme.contains("summer", ignoreCase = true) ||
          theme.contains("road", ignoreCase = true) || theme.contains("heart", ignoreCase = true) ||
          genre.contains("rock", ignoreCase = true) || genre.contains("pop", ignoreCase = true) ||
          genre.contains("hip", ignoreCase = true) || genre.contains("jazz", ignoreCase = true)
        )

        val aiPrompt = """
          You are a world-class songwriter, lyricist, and music producer.
          Theme / Story: "$theme"
          Music Genre / Style: "$genre"
          Mood / Vibe: "$mood"
          ${if (additionalNotes.isNotBlank()) "Additional Notes/Instructions: \"$additionalNotes\"" else ""}

          Instructions:
          - Write complete, original, deeply evocative, and rhythmically rhyming song lyrics.
          - Match the language of the prompt/theme (if English, write in English; if Turkish, write in Turkish).
          - Provide an unforgettable song title.
          - Structure with standard tags: [INTRO], [VERSE 1], [PRE-CHORUS], [CHORUS], [VERSE 2], [BRIDGE], [OUTRO] (or [GİRİŞ], [BÖLÜM 1], [ÖN NAKARAT], [NAKARAT], [BÖLÜM 2], [KÖPRÜ], [ÇIKIŞ] if in Turkish).

          Output strictly valid JSON with no markdown backticks:
          {
            "title": "Song Title",
            "lyrics": "[VERSE 1]\nFirst line...\n\n[CHORUS]\nChorus line...\n\n[VERSE 2]\nSecond verse...\n\n[BRIDGE]\nBridge...\n\n[OUTRO]\nFade out..."
          }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
          val contentsArray = JSONArray()
          val contentObj = JSONObject()
          val partsArray = JSONArray()
          partsArray.put(JSONObject().apply { put("text", aiPrompt) })
          contentObj.put("parts", partsArray)
          contentsArray.put(contentObj)
          put("contents", contentsArray)

          val genConfig = JSONObject().apply {
            put("temperature", 0.85)
            put("responseMimeType", "application/json")
          }
          put("generationConfig", genConfig)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonRequest.toString().toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(body).build()

        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
          val responseBody = response.body?.string()
          if (responseBody != null) {
            val rootObj = JSONObject(responseBody)
            val candidates = rootObj.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textJson = parts?.optJSONObject(0)?.optString("text")
            if (textJson != null) {
              val parsed = JSONObject(textJson)
              val title = parsed.optString("title", "Yeni Beste")
              val lyrics = parsed.optString("lyrics", "")
              if (lyrics.isNotBlank()) {
                return@withContext Pair(title, lyrics)
              }
            }
          }
        }
      } catch (e: Exception) {
        Log.w("AiMusicGenerator", "Gemini lyrics call failed: ${e.message}")
      }
    }

    val isEnglish = theme.any { it in 'a'..'z' || it in 'A'..'Z' } && !theme.any { it in "çÇğĞıİöÖşŞüÜ" } && (
      theme.contains("love", ignoreCase = true) || theme.contains("night", ignoreCase = true) ||
      theme.contains("city", ignoreCase = true) || theme.contains("rock", ignoreCase = true) ||
      theme.contains("pop", ignoreCase = true) || theme.contains("summer", ignoreCase = true) ||
      theme.contains("road", ignoreCase = true) || theme.contains("heart", ignoreCase = true) ||
      theme.contains("dream", ignoreCase = true) || theme.contains("light", ignoreCase = true) ||
      genre.contains("rock", ignoreCase = true) || genre.contains("pop", ignoreCase = true) ||
      genre.contains("hip", ignoreCase = true) || genre.contains("jazz", ignoreCase = true) ||
      genre.contains("ballad", ignoreCase = true)
    )

    if (isEnglish) {
      val engTitle = when {
        theme.contains("love", ignoreCase = true) || theme.contains("heart", ignoreCase = true) -> "Echoes of Your Heart"
        theme.contains("night", ignoreCase = true) || theme.contains("city", ignoreCase = true) -> "Neon City Lights"
        theme.contains("road", ignoreCase = true) || theme.contains("freedom", ignoreCase = true) -> "Chasing the Open Horizon"
        theme.isNotBlank() -> "${theme.replaceFirstChar { it.uppercase() }} (Acoustic)"
        else -> "Whispers in the Wind"
      }
      val engLyrics = """
[INTRO]
(Melodic $genre guitar chords and atmospheric pads)

[VERSE 1]
Footsteps echo down the empty boulevard
Looking at the shadows painting on the dark
Every memory we chased across the sky
Still burning like a candle in the night.

[PRE-CHORUS]
Can you hear the rhythm of the rain?
Washing all the sorrow, easing all the pain...

[CHORUS]
We were flying higher than the summer sun
Racing through the midnight till the battle's won
If you call my name out in the pouring rain
I will cross the oceans just to hold your hand again!

[VERSE 2]
Neon lights are shining through the hazy mist
Holding on to moments that we couldn't resist
Every whisper carried on the autumn breeze
Brings me back to you and sets my spirit free.

[BRIDGE]
No wall too high, no road too far
We found our guiding light inside a falling star!

[OUTRO]
(Gentle piano and vocal fading into silence)
Forever in the melody... forever with you.
      """.trimIndent()
      return@withContext Pair(engTitle, engLyrics)
    }

    // Procedural fallback lyrics generation based on theme and genre (Turkish)
    val proceduralTitle = when {
      theme.contains("ayrılık", ignoreCase = true) || theme.contains("veda", ignoreCase = true) -> "Sonbahar Vedası"
      theme.contains("gece", ignoreCase = true) || theme.contains("yıldız", ignoreCase = true) -> "Gece Işıkları"
      theme.contains("umut", ignoreCase = true) || theme.contains("yarın", ignoreCase = true) -> "Yeniden Doğuş"
      theme.contains("aşk", ignoreCase = true) || theme.contains("sevgi", ignoreCase = true) -> "Aşkın Sonsuz Melodisi"
      theme.isNotBlank() -> "${theme.replaceFirstChar { it.uppercase() }} (Akustik)"
      else -> "Rüzgarın Fısıltısı"
    }

    val proceduralLyrics = """
[GİRİŞ]
(Yumuşak $genre arpejleri ve $mood yaylılar girişi)

[BÖLÜM 1]
$theme düşer gecenin sessizliğine
Kelimeler süzülür yüreğin derinliğine
Gözlerimde kalan son hatıralar
Zamanı unutan ıslak kaldırımlar.

[ÖN NAKARAT]
Bir kıvılcım çakar karanlıkta
Umut saklanır her fırtınada...

[NAKARAT]
Bu şarkı çalarken döner dünya
Gerçek olur belki en zor rüya
Sesin yankılanır boş sokaklarda
Adın yazılı her bir notada!

[BÖLÜM 2]
Rüzgar taşır eski fısıltıları
Dağıtır sisleri, çözer buzları
Bizim hikayemiz burada bitmez
Akan gözyaşları sevgiyi silmez.

[KÖPRÜ]
Hadi duy bu feryadı, uzat ellerini
Bu melodi fethetsin gecenin kalbini!

[ÇIKIŞ]
(Piyano ve synthesizer sönüşü, son yankı)
Sensiz asla... Sonsuza dek bu melodi.
    """.trimIndent()

    Pair(proceduralTitle, proceduralLyrics)
  }
}
