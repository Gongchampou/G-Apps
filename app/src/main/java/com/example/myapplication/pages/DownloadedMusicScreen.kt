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
 * DOWNLOADED MUSIC SCREEN
 * This page is like a "File Manager" specifically for your music.
 * It shows only the songs you've downloaded and lets you delete them to save space.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedMusicScreen(onBack: () -> Unit) {
    
    // --- APP TOOLS ---
    val context = LocalContext.current // Helps the app find the hidden "music" folder on your phone
    
    // --- DATA HOLDER ---
    // This list will hold the songs that are physically saved on your phone.
    var downloadedTracks by remember { mutableStateOf<List<Track>>(emptyList()) }

    // --- MUSIC ENGINE CONNECTION ---
    // We connect to the music player so that if you delete a song while it's playing, 
    // the app can stop the music properly.
    val controllerFuture = remember {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        MediaController.Builder(context, sessionToken).buildAsync()
    }
    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    // Connects the screen to the background player "engine"
    LaunchedEffect(controllerFuture) {
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
        }, MoreExecutors.directExecutor())
    }

    // Safely disconnects when you leave the page to save battery
    DisposableEffect(Unit) {
        onDispose {
            MediaController.releaseFuture(controllerFuture)
        }
    }
    
    // --- FILE HELPERS ---
    // Logic to name the files correctly (e.g., "track_1.mp3")
    fun getFileName(track: Track): String {
        return if (track.url.isBlank()) {
            track.fileName
        } else {
            "track_${track.id}.mp3"
        }
    }

    /**
     * loadDownloadedTracks: This is the "Audit" function. 
     * It checks your phone's storage and builds the list of what it finds.
     */
    fun loadDownloadedTracks() {
        try {
            // 1. Open the master list of all possible songs
            val jsonString = context.assets.open("music_list.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Track>>() {}.type
            val allTracks: List<Track> = Gson().fromJson(jsonString, type)
            
            // 2. Find the folder where music is stored
            val musicDir = File(context.filesDir, "music")
            if (!musicDir.exists()) musicDir.mkdirs()

            // 3. CLEANUP: Delete any files that are broken (0 bytes) or shouldn't be there
            val validFileNames = allTracks.map { getFileName(it) }.toSet()
            musicDir.listFiles()?.forEach { file ->
                if (!validFileNames.contains(file.name) || file.length() <= 0L) {
                    file.delete()
                }
            }

            // 4. UPDATE UI: Refresh the list you see on screen
            downloadedTracks = allTracks.filter { track ->
                val file = File(musicDir, getFileName(track))
                track.url.isNotBlank() && file.exists() && file.length() > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Runs the "Audit" as soon as you open this page
    LaunchedEffect(Unit) {
        loadDownloadedTracks()
    }

    // --- UI DESIGN SECTION ---
    Scaffold(
        topBar = {
            TopAppBar(
                // CHANGE: Change the title of the page here
                title = { Text("Downloaded Music") },
                navigationIcon = {
                    // Back arrow button to go back to Settings or Music page
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            
            // If you haven't downloaded anything yet, show this message
            if (downloadedTracks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No downloaded music found.", color = Color.Gray)
                }
            } else {
                // THE LIST: Shows all your saved songs
                LazyColumn {
                    items(downloadedTracks) { track ->
                        // One individual song row
                        ListItem(
                            headlineContent = { Text(track.title, fontWeight = FontWeight.Bold) },
                            supportingContent = { Text(track.artist) },
                            trailingContent = {
                                // THE DELETE BUTTON (TRASH CAN)
                                IconButton(onClick = {
                                    val controller = mediaController
                                    
                                    // SAFETY: If you are deleting the song you are currently hearing, stop the music!
                                    if (controller != null && controller.currentMediaItem?.mediaId == track.id.toString()) {
                                        controller.stop()
                                    }

                                    // DELETE: Physically erase the file from your phone's memory
                                    val file = File(File(context.filesDir, "music"), getFileName(track))
                                    if (file.exists()) {
                                        file.delete()
                                        // Immediately refresh the list so the song disappears from the screen
                                        loadDownloadedTracks()
                                    }
                                }) {
                                    // CHANGE: Customize the trash can color here
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        )
                        HorizontalDivider() // Draws a thin line between songs
                    }
                }
            }
        }
    }
}
