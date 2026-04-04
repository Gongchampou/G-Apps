package com.example.myapplication.pages

import android.content.ComponentName
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.myapplication.PlaybackService
import com.example.myapplication.Track
import com.google.common.util.concurrent.MoreExecutors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.myapplication.Screen

/**
 * MusicScreen: The primary local playback screen for the Focus Music player.
 * Displays only downloaded or pre-installed music and manages audio playback state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(navController: NavController) {
    // INSTRUCTION: Setup UI contexts and reactive state holders
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val allTracks = remember { mutableStateListOf<Track>() }
    var currentTrack by remember { mutableStateOf<Track?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    
    // INSTRUCTION: Category filter options for user navigation
    val categories = listOf("All", "Study", "Sleep", "Relaxation", "Work", "Focus")
    var selectedCategory by remember { mutableStateOf("All") }
    
    // INSTRUCTION: Refresh mechanism to detect when user returns from Online Library
    var refreshTrigger by remember { mutableStateOf(0) }

    // INSTRUCTION: Establish connection to the persistent PlaybackService
    val controllerFuture = remember {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        MediaController.Builder(context, sessionToken).buildAsync()
    }
    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    // INSTRUCTION: Automatically trigger a refresh whenever the screen gains focus
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // INSTRUCTION: Bind the media controller once the future resolves
    LaunchedEffect(controllerFuture) {
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
        }, MoreExecutors.directExecutor())
    }

    // INSTRUCTION: Cleanup the media session connection on teardown
    DisposableEffect(Unit) {
        onDispose {
            MediaController.releaseFuture(controllerFuture)
        }
    }

    // INSTRUCTION: Sync the UI's 'isPlaying' and 'currentTrack' states with the MediaSession
    DisposableEffect(mediaController) {
        val controller = mediaController ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                isPlaying = isPlayingChanged
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId
                if (mediaId != null) {
                    val track = allTracks.find { it.id.toString() == mediaId }
                    if (track != null && track != currentTrack) {
                        currentTrack = track
                    }
                }
            }
        }
        controller.addListener(listener)
        isPlaying = controller.isPlaying
        onDispose {
            controller.removeListener(listener)
        }
    }

    // INSTRUCTION: Map track metadata to physical file storage paths
    fun getFileName(track: Track): String {
        return if (track.url.isBlank()) {
            track.fileName
        } else {
            "track_${track.id}.mp3"
        }
    }

    // INSTRUCTION: Verify track presence in assets or local downloads
    fun isDownloaded(track: Track): Boolean {
        if (track.url.isBlank()) return true // Assets are always "available"
        val file = File(File(context.filesDir, "music"), getFileName(track))
        return file.exists() && file.length() > 0
    }

    // INSTRUCTION: React to track changes by preparing the MediaController with the new URI
    LaunchedEffect(currentTrack, mediaController) {
        val controller = mediaController ?: return@LaunchedEffect
        currentTrack?.let { track ->
            try {
                if (controller.currentMediaItem?.mediaId == track.id.toString()) {
                    return@LaunchedEffect
                }

                val mediaItem = if (track.url.isNotBlank()) {
                    val fileName = getFileName(track)
                    val file = File(File(context.filesDir, "music"), fileName)
                    val uri = if (file.exists() && file.length() > 0) {
                        file.toURI().toString()
                    } else {
                        track.url
                    }
                    MediaItem.Builder()
                        .setUri(uri)
                        .setMediaId(track.id.toString())
                        .build()
                } else {
                    MediaItem.Builder()
                        .setUri("asset:///music/${track.fileName}")
                        .setMediaId(track.id.toString())
                        .build()
                }

                controller.setMediaItem(mediaItem)
                controller.prepare()
                if (isPlaying) {
                    controller.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // INSTRUCTION: Propagate play/pause UI actions directly to the MediaController
    LaunchedEffect(isPlaying, mediaController) {
        val controller = mediaController ?: return@LaunchedEffect
        if (isPlaying) {
            if (currentTrack == null && allTracks.isNotEmpty()) {
                val downloaded = allTracks.filter { isDownloaded(it) }
                if (downloaded.isNotEmpty()) currentTrack = downloaded[0]
            }
            if (controller.playbackState == Player.STATE_IDLE) {
                controller.prepare()
            }
            controller.play()
        } else {
            controller.pause()
        }
    }

    /**
     * Data Refresh Logic: Loads the JSON catalog and filters for available music.
     * Ensures only existing files are displayed in the Focus list.
     */
    LaunchedEffect(refreshTrigger) {
        try {
            val jsonString = context.assets.open("music_list.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Track>>() {}.type
            val loadedTracks: List<Track> = Gson().fromJson(jsonString, type)
            allTracks.clear()
            allTracks.addAll(loadedTracks)
            
            // INSTRUCTION: Fallback - if the current track was deleted, pick a new one
            if (currentTrack != null && !isDownloaded(currentTrack!!)) {
                currentTrack = allTracks.firstOrNull { isDownloaded(it) }
            } else if (currentTrack == null) {
                currentTrack = allTracks.firstOrNull { isDownloaded(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // INSTRUCTION: Filter logic - derived from the master list but limited to downloads
    val visibleTracks = remember(allTracks.size, refreshTrigger) {
        allTracks.filter { isDownloaded(it) }
    }
    
    val filteredTracks = remember(selectedCategory, visibleTracks) {
        if (selectedCategory == "All") visibleTracks else visibleTracks.filter { it.category == selectedCategory }
    }

    // INSTRUCTION: Main UI Layout
    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Focus Music", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { navController.navigate(Screen.OnlineMusic.route) }) {
                Icon(Icons.Default.Download, "Online Library")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // INSTRUCTION: Persistent Playback Control Card
        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF5856D6))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Now Playing: ${currentTrack?.title ?: "Select a track"}", color = Color.White, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Skip Previous functionality
                        IconButton(onClick = {
                            val currentIndex = visibleTracks.indexOf(currentTrack)
                            if (currentIndex > 0) {
                                currentTrack = visibleTracks[currentIndex - 1]
                            } else if (visibleTracks.isNotEmpty()) {
                                currentTrack = visibleTracks.last()
                            }
                        }) { Icon(Icons.Default.SkipPrevious, "", tint = Color.White) }
                        
                        // Play/Pause central button
                        IconButton(onClick = { isPlaying = !isPlaying }, modifier = Modifier.size(64.dp)) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                                "", 
                                tint = Color.White, 
                                modifier = Modifier.size(48.dp)
                            ) 
                        }
                        
                        // Skip Next functionality
                        IconButton(onClick = {
                            val currentIndex = visibleTracks.indexOf(currentTrack)
                            if (currentIndex < visibleTracks.size - 1) {
                                currentTrack = visibleTracks[currentIndex + 1]
                            } else if (visibleTracks.isNotEmpty()) {
                                currentTrack = visibleTracks.first()
                            }
                        }) { Icon(Icons.Default.SkipNext, "", tint = Color.White) }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // INSTRUCTION: Category Filter Row for quick navigation
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        // INSTRUCTION: Handle empty local library - direct user to online source
        if (visibleTracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No local tracks found.", color = Color.Gray)
                    Button(onClick = { navController.navigate(Screen.OnlineMusic.route) }) {
                        Text("Download from Online Library")
                    }
                }
            }
        } else {
            // INSTRUCTION: List of available tracks to play
            LazyColumn {
                items(filteredTracks) { track ->
                    ListItem(
                        headlineContent = { Text(track.title) },
                        supportingContent = { Text(track.artist) },
                        trailingContent = { 
                            IconButton(onClick = {
                                if (currentTrack == track) {
                                    isPlaying = !isPlaying
                                } else {
                                    currentTrack = track
                                    isPlaying = true
                                }
                            }) {
                                Icon(
                                    if (currentTrack == track && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                                    null
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
