package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository pattern implementation for SavedLyrics data access.
 */
class LyricsRepository(private val savedLyricsDao: SavedLyricsDao) {
  val allSavedLyrics: Flow<List<SavedLyrics>> = savedLyricsDao.getAllSavedLyrics()

  suspend fun saveLyrics(title: String, prompt: String, lyrics: String): Long {
    return savedLyricsDao.insertLyrics(
      SavedLyrics(
        title = title.ifBlank { "Untitled Song" },
        prompt = prompt,
        lyrics = lyrics,
        timestamp = System.currentTimeMillis()
      )
    )
  }

  suspend fun deleteLyricsById(id: Long) {
    savedLyricsDao.deleteLyricsById(id)
  }

  suspend fun deleteLyrics(lyrics: SavedLyrics) {
    savedLyricsDao.deleteLyrics(lyrics)
  }
}
