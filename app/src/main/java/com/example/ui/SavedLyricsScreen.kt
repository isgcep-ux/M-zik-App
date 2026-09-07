package com.example.ui

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.LyricsRepository
import com.example.data.SavedLyrics
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanDark
import com.example.ui.theme.StudioCyanGlow
import com.example.ui.theme.StudioDivider
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceCard
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.StudioTurquoiseTint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for managing and observing SavedLyrics from the Room Database.
 */
class SavedLyricsViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: LyricsRepository
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  init {
    val db = AppDatabase.getDatabase(application)
    repository = LyricsRepository(db.savedLyricsDao())
  }

  // Reactive Flow of all saved compositions from Room DB
  val savedLyricsList: StateFlow<List<SavedLyrics>> = repository.allSavedLyrics
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  fun onSearchQueryChanged(query: String) {
    _searchQuery.update { query }
  }

  fun deleteLyric(id: Long) {
    viewModelScope.launch {
      repository.deleteLyricsById(id)
    }
  }
}

/**
 * Screen displaying the user's saved song lyrics fetched from the local Room database.
 */
@Composable
fun SavedLyricsScreen(
  viewModel: SavedLyricsViewModel = viewModel(),
  onBack: () -> Unit,
  onSelectLyric: ((SavedLyrics) -> Unit)? = null,
  onNavigateToStudio: () -> Unit,
  modifier: Modifier = Modifier
) {
  val savedLyrics by viewModel.savedLyricsList.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val clipboardManager = LocalClipboardManager.current
  var itemToDelete by remember { mutableStateOf<SavedLyrics?>(null) }
  var copiedId by remember { mutableStateOf<Long?>(null) }
  var expandedId by remember { mutableStateOf<Long?>(null) }

  // Filter lyrics by search query
  val filteredLyrics = remember(savedLyrics, searchQuery) {
    if (searchQuery.isBlank()) {
      savedLyrics
    } else {
      savedLyrics.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
          it.prompt.contains(searchQuery, ignoreCase = true) ||
          it.lyrics.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(StudioBackground),
    containerColor = StudioBackground
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(innerPadding)
        .padding(horizontal = 18.dp, vertical = 12.dp)
        .testTag("saved_lyrics_screen_container"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Top Navigation Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = StudioSurfaceVariant,
            border = BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier
              .clickable { onBack() }
              .testTag("saved_lyrics_back_button")
          ) {
            Box(
              modifier = Modifier.size(40.dp),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = StudioCyanDark,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Column {
            Text(
              text = "Saved Compositions",
              fontSize = 19.sp,
              fontWeight = FontWeight.ExtraBold,
              color = StudioTextPrimary
            )
            Text(
              text = "Room Database Library",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = StudioCyanDark
            )
          }
        }

        // Count Badge
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = StudioTurquoiseTint,
          border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Bookmark,
              contentDescription = null,
              tint = StudioCyanDark,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${savedLyrics.size} Songs",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = StudioCyanDark
            )
          }
        }
      }

      // Search and Filter Bar
      if (savedLyrics.isNotEmpty()) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.onSearchQueryChanged(it) },
          placeholder = {
            Text(
              "Search saved titles, genres, or lyrics...",
              color = StudioTextTertiary,
              fontSize = 13.sp
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = StudioTextTertiary,
              modifier = Modifier.size(18.dp)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear search",
                  tint = StudioTextTertiary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_lyrics_search_field"),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.45f),
            unfocusedContainerColor = StudioSurfaceVariant.copy(alpha = 0.45f),
            focusedBorderColor = StudioCyan,
            unfocusedBorderColor = StudioCardBorder,
            focusedTextColor = StudioTextPrimary,
            unfocusedTextColor = StudioTextPrimary
          )
        )
      }

      // Main List View / Empty State
      if (savedLyrics.isEmpty()) {
        // Empty State: No Compositions in Database
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("saved_lyrics_empty_state"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
          border = BorderStroke(1.dp, StudioCardBorder)
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(StudioTurquoiseTint),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = StudioCyanDark,
                modifier = Modifier.size(34.dp)
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No Saved Lyrics Yet",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Compose songs with Gemini AI in Lyric Studio and tap 'Save' to preserve them in your offline Room database.",
              fontSize = 13.sp,
              color = StudioTextSecondary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
              onClick = onNavigateToStudio,
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = StudioCyan),
              modifier = Modifier.testTag("empty_state_create_button")
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Write Song in Studio",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }
        }
      } else if (filteredLyrics.isEmpty()) {
        // Search Filter Empty State
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
          border = BorderStroke(1.dp, StudioCardBorder)
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = StudioTextTertiary,
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No matches for \"$searchQuery\"",
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = StudioTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Check the spelling or try searching for another genre or lyric snippet.",
              fontSize = 12.sp,
              color = StudioTextTertiary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        // Populated LazyColumn of SavedLyrics
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("saved_lyrics_list"),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(filteredLyrics, key = { it.id }) { item ->
            val isExpanded = expandedId == item.id
            val isCopied = copiedId == item.id

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable {
                  expandedId = if (isExpanded) null else item.id
                }
                .testTag("saved_lyrics_card_${item.id}"),
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = StudioSurfaceCard),
              border = BorderStroke(
                1.dp,
                if (isExpanded) StudioCyan.copy(alpha = 0.8f) else StudioCardBorder
              ),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                // Header: Title & Expand Indicator
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioTurquoiseTint),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = StudioCyanDark,
                        modifier = Modifier.size(18.dp)
                      )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = dateFormat.format(Date(item.timestamp)),
                        fontSize = 10.sp,
                        color = StudioTextTertiary
                      )
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    // Prompt/Genre Chip
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = StudioSurfaceVariant,
                      border = BorderStroke(1.dp, StudioCardBorder)
                    ) {
                      Text(
                        text = item.prompt.take(18) + if (item.prompt.length > 18) "…" else "",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = StudioCyanDark,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                      )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                      imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                      contentDescription = if (isExpanded) "Collapse" else "Expand",
                      tint = StudioTextTertiary,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }

                // Short Snippet when collapsed
                if (!isExpanded) {
                  val previewLines = item.lyrics.lines().filter { it.isNotBlank() }.take(2).joinToString("\n")
                  if (previewLines.isNotBlank()) {
                    Text(
                      text = previewLines,
                      fontSize = 12.sp,
                      lineHeight = 17.sp,
                      color = StudioTextSecondary,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.padding(start = 42.dp)
                    )
                  }
                }

                // Full Lyrics and Actions when expanded
                AnimatedVisibility(
                  visible = isExpanded,
                  enter = expandVertically() + fadeIn(),
                  exit = shrinkVertically() + fadeOut()
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    // Full scrollable lyrics container
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioSurfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                    ) {
                      val scrollState = rememberScrollState()
                      Column(
                        modifier = Modifier
                          .fillMaxSize()
                          .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                      ) {
                        item.lyrics.lines().forEach { line ->
                          val trimmed = line.trim()
                          if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                            Text(
                              text = trimmed,
                              fontSize = 11.sp,
                              fontWeight = FontWeight.ExtraBold,
                              fontFamily = FontFamily.Monospace,
                              color = StudioCyanDark,
                              modifier = Modifier.padding(top = 4.dp)
                            )
                          } else if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
                            Text(
                              text = trimmed,
                              fontSize = 11.sp,
                              color = StudioTextTertiary
                            )
                          } else {
                            Text(
                              text = line,
                              fontSize = 12.sp,
                              lineHeight = 18.sp,
                              color = StudioTextPrimary
                            )
                          }
                        }
                      }
                    }

                    // Card Action Row: Copy, Load to Studio, Delete
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Copy Button
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = if (isCopied) StudioTurquoiseTint else StudioSurfaceVariant,
                          modifier = Modifier
                            .clickable {
                              val fullText = "${item.title}\n\n${item.lyrics}"
                              clipboardManager.setText(AnnotatedString(fullText))
                              copiedId = item.id
                            }
                            .testTag("copy_saved_lyric_${item.id}")
                        ) {
                          Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Icon(
                              imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                              contentDescription = "Copy",
                              tint = if (isCopied) StudioCyanDark else StudioTextSecondary,
                              modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = if (isCopied) "Copied" else "Copy",
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold,
                              color = if (isCopied) StudioCyanDark else StudioTextSecondary
                            )
                          }
                        }

                        // Load into Studio Button
                        if (onSelectLyric != null) {
                          Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioTurquoiseTint,
                            border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.5f)),
                            modifier = Modifier
                              .clickable {
                                onSelectLyric(item)
                              }
                              .testTag("load_into_studio_${item.id}")
                          ) {
                            Row(
                              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                              verticalAlignment = Alignment.CenterVertically
                            ) {
                              Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Load in Studio",
                                tint = StudioCyanDark,
                                modifier = Modifier.size(14.dp)
                              )
                              Spacer(modifier = Modifier.width(4.dp))
                              Text(
                                text = "Load in Studio",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioCyanDark
                              )
                            }
                          }
                        }
                      }

                      // Delete Button
                      IconButton(
                        onClick = { itemToDelete = item },
                        modifier = Modifier
                          .size(32.dp)
                          .testTag("delete_saved_button_${item.id}")
                      ) {
                        Icon(
                          imageVector = Icons.Default.Delete,
                          contentDescription = "Delete composition",
                          tint = StudioTextTertiary,
                          modifier = Modifier.size(18.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Delete Confirmation Dialog
  if (itemToDelete != null) {
    val target = itemToDelete!!
    AlertDialog(
      onDismissRequest = { itemToDelete = null },
      title = {
        Text(
          text = "Delete Composition?",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = StudioTextPrimary
        )
      },
      text = {
        Text(
          text = "Are you sure you want to remove \"${target.title}\" from your saved Room database?",
          fontSize = 13.sp,
          color = StudioTextSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteLyric(target.id)
            if (expandedId == target.id) expandedId = null
            itemToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Delete", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToDelete = null }) {
          Text("Cancel", color = StudioTextSecondary, fontSize = 12.sp)
        }
      },
      containerColor = StudioSurfaceCard,
      shape = RoundedCornerShape(18.dp)
    )
  }
}
