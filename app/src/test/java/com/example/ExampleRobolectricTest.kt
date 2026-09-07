package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.SavedLyrics
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI Müzik", appName)
  }

  @Test
  fun `insert and retrieve saved lyrics from Room database`() = runBlocking {
    val dao = database.savedLyricsDao()
    val testLyrics = SavedLyrics(
      title = "Midnight Odyssey",
      prompt = "Synthwave 80s",
      lyrics = "[VERSE 1]\nNeon lights in the rain..."
    )

    val id = dao.insertLyrics(testLyrics)
    assertNotNull(id)

    val allLyrics = dao.getAllSavedLyrics().first()
    assertEquals(1, allLyrics.size)
    assertEquals("Midnight Odyssey", allLyrics[0].title)
    assertEquals("Synthwave 80s", allLyrics[0].prompt)
    assertEquals("[VERSE 1]\nNeon lights in the rain...", allLyrics[0].lyrics)
  }
}
