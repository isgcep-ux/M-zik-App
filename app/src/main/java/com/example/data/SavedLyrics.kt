package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing saved song lyrics.
 */
@Entity(tableName = "saved_lyrics")
data class SavedLyrics(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val prompt: String,
  val lyrics: String,
  val timestamp: Long = System.currentTimeMillis()
)
