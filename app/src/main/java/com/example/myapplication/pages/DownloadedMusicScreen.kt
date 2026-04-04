package com.example.myapplication.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.content.ComponentName
import com.example.myapplication.PlaybackService
import com.example.myapplication.Track
import com.google.common.util.concurrent.MoreExecutors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * DownloadedMusicScreen: Provides a dedicated view for managing only local music files.
 * Includes a background cleanup audit to purge any unrecognized or corrupted music data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedMusicScreen(onBack: () -> Unit) {
    // INSTRUCTION: Access system context and manage track state
    val context = LocalContext.current
    var downloadedTracks by remember { mutableStateOf<List<Track>>(emptyList()) }

    // INSTRUCTION: Initialize MediaController to stop playback if a song is deleted while playing
    val controllerFuture = remember {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        MediaController.Builder(context, sessionToken).buildAsync()
    }
    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    // INSTRUCTION: Setup listener to receive the MediaController instance once ready
    LaunchedEffect(controllerFuture) {
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
        }, MoreExecutors.directExecutor())
    }

    // INSTRUCTION: Clean up MediaController future when the screen is dismissed
    DisposableEffect(Unit) {
        onDispose {
            MediaController.releaseFuture(controllerFuture)
        }
    }
    
    // INSTRUCTION: Utility to match track ID with the local .mp3 filename
    fun getFileName(track: Track): String {
        return if (track.url.isBlank()) {
            track.fileName
        } else {
            "track_${track.id}.mp3"
        }
    }

    /**
     * loadDownloadedTracks: Audits the storage directory and refreshes the UI list.
     * This function physically deletes files that are not in the valid JSON list or are empty.
     */
    fun loadDownloadedTracks() {
        try {
            // INSTRUCTION: Parse the master music list from assets
            val jsonString = context.assets.open("music_list.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Track>>() {}.type
            val allTracks: List<Track> = Gson().fromJson(jsonString, type)
            val musicDir = File(context.filesDir, "music")
            if (!musicDir.exists()) musicDir.mkdirs()

            // INSTRUCTION: 1. Identify valid filenames based on the catalog JSON
            val validFileNames = allTracks.map { getFileName(it) }.toSet()
            
            // INSTRUCTION: 2. STORAGE CLEANUP: Purge any unknown or empty (0-byte) files immediately
            musicDir.listFiles()?.forEach { file ->
                if (!validFileNames.contains(file.name) || file.length() <= 0L) {
                    file.delete()
                }
            }

            // INSTRUCTION: 3. Filter the list to only show valid downloaded tracks
            downloadedTracks = allTracks.filter { track ->
                val file = File(musicDir, getFileName(track))
                track.url.isNotBlank() && file.exists() && file.length() > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // INSTRUCTION: Initial data load and audit when entering the screen
    LaunchedEffect(Unit) {
        loadDownloadedTracks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloaded Music") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // INSTRUCTION: Handle empty state if no tracks have been downloaded
            if (downloadedTracks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No downloaded music found.", color = Color.Gray)
                }
            } else {
                // INSTRUCTION: List of downloaded tracks with individual delete controls
                LazyColumn {
                    items(downloadedTracks) { track ->
                        ListItem(
                            headlineContent = { Text(track.title, fontWeight = FontWeight.Bold) },
                            supportingContent = { Text(track.artist) },
                            trailingContent = {
                                IconButton(onClick = {
                                    val controller = mediaController
                                    // INSTRUCTION: Safety - Stop playback if we are deleting the current song
                                    if (controller != null && controller.currentMediaItem?.mediaId == track.id.toString()) {
                                        controller.stop()
                                    }

                                    // INSTRUCTION: Physical file removal from storage
                                    val file = File(File(context.filesDir, "music"), getFileName(track))
                                    if (file.exists()) {
                                        file.delete()
                                        // INSTRUCTION: Immediately refresh the list to update UI and settings count
                                        loadDownloadedTracks()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
