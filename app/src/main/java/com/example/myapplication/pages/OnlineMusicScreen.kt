package com.example.myapplication.pages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * OnlineMusicScreen: Provides a browseable library of music available for download.
 * Users can preview tracks, download them to local storage, or delete existing downloads.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineMusicScreen(onBack: () -> Unit) {
    // INSTRUCTION: Local context for file and asset access
    val context = LocalContext.current
    // INSTRUCTION: Coroutine scope for handling asynchronous downloads
    val scope = rememberCoroutineScope()
    // INSTRUCTION: List of all tracks parsed from the online catalog
    val tracks = remember { mutableStateListOf<Track>() }
    // INSTRUCTION: Reactive maps to track which files are downloaded or currently downloading
    val downloadedFiles = remember { mutableStateMapOf<String, Boolean>() }
    val downloadingProgress = remember { mutableStateMapOf<String, Float>() }
    // INSTRUCTION: State to manage the deletion confirmation dialog
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

    // INSTRUCTION: Utility to generate a consistent filename based on track ID
    fun getFileName(track: Track): String = "track_${track.id}.mp3"

    // INSTRUCTION: Check if a track's file exists and is valid (non-empty)
    fun isDownloaded(track: Track): Boolean {
        val file = File(File(context.filesDir, "music"), getFileName(track))
        return file.exists() && file.length() > 0
    }

    // INSTRUCTION: Refresh the entire downloaded status map to sync UI with storage
    fun refreshDownloadedStatus() {
        tracks.forEach { downloadedFiles[it.id.toString()] = isDownloaded(it) }
    }

    // INSTRUCTION: Load the music list from assets on initial composition
    LaunchedEffect(Unit) {
        try {
            val jsonString = context.assets.open("music_list.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Track>>() {}.type
            val allTracks: List<Track> = Gson().fromJson(jsonString, type)
            // INSTRUCTION: Only online tracks (those with a URL) are shown here
            tracks.addAll(allTracks.filter { it.url.isNotBlank() })
            refreshDownloadedStatus()
        } catch (e: Exception) { e.printStackTrace() }
    }

    /**
     * downloadTrack: Handles the network I/O to fetch the MP3 file.
     * Includes progress tracking and automatic cleanup of partial files on error.
     */
    suspend fun downloadTrack(track: Track) {
        downloadingProgress[track.id.toString()] = 0f
        val musicDir = File(context.filesDir, "music").apply { if (!exists()) mkdirs() }
        val file = File(musicDir, getFileName(track))

        withContext(Dispatchers.IO) {
            try {
                val connection = URL(track.url).openConnection()
                val totalSize = connection.contentLength.toLong()
                connection.getInputStream().use { input ->
                    file.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (totalSize > 0) {
                                withContext(Dispatchers.Main) {
                                    downloadingProgress[track.id.toString()] = totalBytesRead.toFloat() / totalSize
                                }
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) { downloadedFiles[track.id.toString()] = true }
            } catch (e: Exception) { 
                e.printStackTrace()
                if (file.exists()) file.delete() // INSTRUCTION: Clean up partial data if download fails
            }
            finally { withContext(Dispatchers.Main) { downloadingProgress.remove(track.id.toString()) } }
        }
    }

    // INSTRUCTION: Confirmation Dialog to prevent accidental deletion of music data
    if (trackToDelete != null) {
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            title = { Text("Delete Track?") },
            text = { Text("This will permanently remove '${trackToDelete?.title}' from your device storage.") },
            confirmButton = {
                TextButton(onClick = {
                    val file = File(File(context.filesDir, "music"), getFileName(trackToDelete!!))
                    if (file.exists()) file.delete()
                    refreshDownloadedStatus()
                    trackToDelete = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Online Library") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(tracks) { track ->
                val isDownloaded = downloadedFiles[track.id.toString()] ?: false
                val progress = downloadingProgress[track.id.toString()]
                
                ListItem(
                    headlineContent = { Text(track.title) },
                    supportingContent = { Text(track.artist) },
                    trailingContent = {
                        // INSTRUCTION: Animated transition between progress bar and action icons
                        AnimatedContent(targetState = progress != null, label = "DownloadAnimation") { isDownloading ->
                            if (isDownloading) {
                                // INSTRUCTION: Professional progress indicator with percentage readout
                                val animatedProgress by animateFloatAsState(targetValue = progress ?: 0f, label = "SmoothProgress")
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                                    CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(30.dp))
                                    Text("${(animatedProgress * 100).toInt()}%", fontSize = 8.sp)
                                }
                            } else {
                                // INSTRUCTION: Toggle button: Downloads if missing, triggers delete if present
                                IconButton(onClick = { 
                                    if (isDownloaded) {
                                        trackToDelete = track
                                    } else {
                                        scope.launch { downloadTrack(track) } 
                                    }
                                }) {
                                    Icon(
                                        if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                        null,
                                        tint = if (isDownloaded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        // All the music i want to add can be add from the (music_list.json) file.
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
