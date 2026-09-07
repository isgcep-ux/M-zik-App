package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SavedLyrics.
 */
@Dao
interface SavedLyricsDao {
  @Query("SELECT * FROM saved_lyrics ORDER BY timestamp DESC")
  fun getAllSavedLyrics(): Flow<List<SavedLyrics>>

  @Query("SELECT * FROM saved_lyrics WHERE id = :id")
  fun getLyricsById(id: Long): Flow<SavedLyrics?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLyrics(lyrics: SavedLyrics): Long

  @Query("DELETE FROM saved_lyrics WHERE id = :id")
  suspend fun deleteLyricsById(id: Long)

  @Delete
  suspend fun deleteLyrics(lyrics: SavedLyrics)
}
