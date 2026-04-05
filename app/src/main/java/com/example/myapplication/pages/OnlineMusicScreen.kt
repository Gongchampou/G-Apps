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
 * ONLINE MUSIC SCREEN
 * This is your "Store" or "Download Center".
 * You can browse all available songs and pick which ones to save to your phone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineMusicScreen(onBack: () -> Unit) {
    
    // --- APP TOOLS ---
    val context = LocalContext.current // Helps find folders on your phone
    val scope = rememberCoroutineScope() // Allows the app to download files "in the background"
    
    // --- DATA HOLDERS ---
    val tracks = remember { mutableStateListOf<Track>() } // The list of songs you see on screen
    val downloadedFiles = remember { mutableStateMapOf<String, Boolean>() } // Keeps track of what is already downloaded
    val downloadingProgress = remember { mutableStateMapOf<String, Float>() } // Tracks the % of a download (0.0 to 1.0)
    
    // FILTER DATA
    val filters = listOf("All", "Downloaded", "Available to Download")
    var selectedFilter by remember { mutableStateOf("All") }

    // State to show the "Are you sure you want to delete?" popup
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

    // --- FILE HELPER ---
    // Naming convention for saved files (e.g., "track_5.mp3")
    fun getFileName(track: Track): String = "track_${track.id}.mp3"

    // Checks if the file is physically on your phone right now
    fun isDownloaded(track: Track): Boolean {
        val file = File(File(context.filesDir, "music"), getFileName(track))
        return file.exists() && file.length() > 0
    }

    // Refresh the checkmarks on the screen
    fun refreshDownloadedStatus() {
        tracks.forEach { downloadedFiles[it.id.toString()] = isDownloaded(it) }
    }

    // FILTER LOGIC: This decides which songs to show based on your tab choice
    val filteredTracks = remember(tracks.size, selectedFilter, downloadedFiles.size) {
        when (selectedFilter) {
            "Downloaded" -> tracks.filter { downloadedFiles[it.id.toString()] == true }
            "Available to Download" -> tracks.filter { downloadedFiles[it.id.toString()] != true }
            else -> tracks // Shows everything
        }
    }

    // --- LOADING THE LIST ---
    // Reads 'music_list.json' to see what songs are available to download
    LaunchedEffect(Unit) {
        try {
            val jsonString = context.assets.open("music_list.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Track>>() {}.type
            val allTracks: List<Track> = Gson().fromJson(jsonString, type)
            
            // Only show songs that have a web link (URL)
            tracks.addAll(allTracks.filter { it.url.isNotBlank() })
            refreshDownloadedStatus()
        } catch (e: Exception) { e.printStackTrace() }
    }

    /**
     * downloadTrack: The "Worker" function that handles the actual download.
     */
    suspend fun downloadTrack(track: Track) {
        // Start progress at 0%
        downloadingProgress[track.id.toString()] = 0f
        
        // Create the folder if it doesn't exist
        val musicDir = File(context.filesDir, "music").apply { if (!exists()) mkdirs() }
        val file = File(musicDir, getFileName(track))

        // Run this part on the "IO" (Input/Output) thread so the screen doesn't freeze
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
                            
                            // Update the percentage circle on the screen
                            if (totalSize > 0) {
                                withContext(Dispatchers.Main) {
                                    downloadingProgress[track.id.toString()] = totalBytesRead.toFloat() / totalSize
                                }
                            }
                        }
                    }
                }
                // Once finished, show the checkmark icon
                withContext(Dispatchers.Main) { downloadedFiles[track.id.toString()] = true }
            } catch (e: Exception) { 
                e.printStackTrace()
                // If it fails, delete the "broken" file so it doesn't take up space
                if (file.exists()) file.delete() 
            }
            finally { 
                // Remove the progress circle when done
                withContext(Dispatchers.Main) { downloadingProgress.remove(track.id.toString()) } 
            }
        }
    }

    // --- DELETE CONFIRMATION POPUP ---
    if (trackToDelete != null) {
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            // CHANGE: Change the title of the popup here
            title = { Text("Delete Track?") },
            text = { Text("This will permanently remove '${trackToDelete?.title}' from your phone.") },
            confirmButton = {
                TextButton(onClick = {
                    val file = File(File(context.filesDir, "music"), getFileName(trackToDelete!!))
                    if (file.exists()) file.delete() // Physically delete
                    refreshDownloadedStatus() // Refresh UI icons
                    trackToDelete = null // Close popup
                }) {
                    // CHANGE: Customize the delete button color
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // --- UI DESIGN SECTION ---
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            
            // TIP: Small back button since the header is gone
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            // --- FILTER TABS ---
            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(selectedFilter),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                filters.forEach { filter ->
                    Tab(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        text = { Text(filter, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // THE LIST: Scrolls up and down
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredTracks) { track ->
                    // Check if this specific song is already downloaded or downloading
                    val isDownloaded = downloadedFiles[track.id.toString()] ?: false
                    val progress = downloadingProgress[track.id.toString()]

                    ListItem(
                        headlineContent = { Text(track.title, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(track.artist) },
                        trailingContent = {
                            // The Right-Hand Icon (Download Button or Progress Circle)
                            AnimatedContent(targetState = progress != null, label = "DownloadAnimation") { isDownloading ->
                                if (isDownloading) {
                                    // SHOW PROGRESS CIRCLE
                                    val animatedProgress by animateFloatAsState(targetValue = progress ?: 0f, label = "SmoothProgress")
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                                        CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(30.dp))
                                        Text("${(animatedProgress * 100).toInt()}%", fontSize = 8.sp) // Show percentage
                                    }
                                } else {
                                    // SHOW DOWNLOAD OR CHECKMARK ICON
                                    IconButton(onClick = {
                                        if (isDownloaded) {
                                            // If already downloaded, clicking it again asks to delete
                                            trackToDelete = track
                                        } else {
                                            // If NOT downloaded, start the download worker
                                            scope.launch { downloadTrack(track) }
                                        }
                                    }) {
                                        Icon(
                                            // Icons change based on status
                                            imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                            contentDescription = null,
                                            // CHANGE: Change colors for Download vs Done status
                                            tint = if (isDownloaded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    )
                    HorizontalDivider() // Line between items
                }
            }
        }
    }
}
