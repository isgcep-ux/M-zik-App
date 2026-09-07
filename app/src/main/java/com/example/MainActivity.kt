package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.AudioPlayerViewModel
import com.example.ui.AudioPlayerScreen
import com.example.ui.LyricsScreen
import com.example.ui.LyricsViewModel
import com.example.ui.SavedLyricsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainAppContent()
      }
    }
  }
}

@Composable
fun MainAppContent() {
  val audioViewModel: AudioPlayerViewModel = viewModel()
  val lyricsViewModel: LyricsViewModel = viewModel()
  var currentScreen by remember { mutableStateOf("lyrics") }

  Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
    when (screen) {
      "lyrics" -> {
        LyricsScreen(
          viewModel = lyricsViewModel,
          onNavigateToPlayer = { currentScreen = "player" },
          onNavigateToSaved = { currentScreen = "saved" }
        )
      }
      "saved" -> {
        SavedLyricsScreen(
          onBack = { currentScreen = "lyrics" },
          onNavigateToStudio = { currentScreen = "lyrics" },
          onSelectLyric = { selected ->
            lyricsViewModel.loadSavedLyric(selected)
            currentScreen = "lyrics"
          }
        )
      }
      else -> {
        AudioPlayerScreen(
          viewModel = audioViewModel,
          onBackToLyrics = { currentScreen = "lyrics" }
        )
      }
    }
  }
}
