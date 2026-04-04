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
import androidx.media3.common.MediaMetadata
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
 * MUSIC SCREEN
 * This is the page where you see and play your downloaded music.
 * Think of this as your "Library" or "Player" page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(navController: NavController) {
    
    // --- APP SMART TOOLS (Don't worry too much about these) ---
    val context = LocalContext.current // Helps the app find files on your phone
    val lifecycleOwner = LocalLifecycleOwner.current // Tells the app if the screen is open or hidden
    
    // --- DATA HOLDERS (These store information while the app is running) ---
    // 'remember' makes the app keep this info even if the screen rotates or updates.
    val allTracks = remember { mutableStateListOf<Track>() } // A list of every song the app knows about
    var currentTrack by remember { mutableStateOf<Track?>(null) } // The song that is currently selected
    var isPlaying by remember { mutableStateOf(false) } // Is the music playing right now? (True or False)
    
    // --- CATEGORIES (THE TABS) ---
    // HOW TO CHANGE: You can add more words inside these quotes to add new tabs!
    // Example: listOf("All", "Study", "Chill", "Rock", "Piano")
    val categories = listOf("All", "Study", "Sleep", "Relaxation", "Work", "Focus")
    
    // Tracks which tab you clicked on. Default is "All".
    var selectedCategory by remember { mutableStateOf("All") }
    
    // A simple trigger to refresh the list when you come back from the download page
    var refreshTrigger by remember { mutableStateOf(0) }

    // --- MUSIC PLAYER ENGINE (Advanced Stuff) ---
    // This connects this screen to the 'PlaybackService' which plays music in the background.
    val controllerFuture = remember {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        MediaController.Builder(context, sessionToken).buildAsync()
    }
    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    // This part refreshes the song list automatically when you open this screen.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshTrigger++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Connects the 'Play' button to the actual music engine
    LaunchedEffect(controllerFuture) {
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
        }, MoreExecutors.directExecutor())
    }

    // Cleans up the connection when you leave the page to save battery
    DisposableEffect(Unit) {
        onDispose { MediaController.releaseFuture(controllerFuture) }
    }

    // Syncs the UI: If the music starts playing, the Play button changes to Pause automatically.
    DisposableEffect(mediaController) {
        val controller = mediaController ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                // CHANGE: We now track 'playWhenReady' instead of 'isPlaying' 
                // to avoid the "work-not work" flickering during song skips.
                isPlaying = playWhenReady
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaId?.let { mediaId ->
                    val track = allTracks.find { it.id.toString() == mediaId }
                    if (track != null && track != currentTrack) currentTrack = track
                }
            }
        }
        controller.addListener(listener)
        isPlaying = controller.playWhenReady
        onDispose { controller.removeListener(listener) }
    }

    // --- FILE HELPERS ---
    // Finds where the music file is stored on your phone.
    fun getFileName(track: Track): String {
        return if (track.url.isBlank()) track.fileName else "track_${track.id}.mp3"
    }

    // Checks if the music file actually exists (so we don't try to play a missing file).
    fun isDownloaded(track: Track): Boolean {
        if (track.url.isBlank()) return true 
        val file = File(File(context.filesDir, "music"), getFileName(track))
        return file.exists() && file.length() > 0
    }

    // Filter logic: Only shows songs that belong to the category you selected.
    val visibleTracks = remember(allTracks.size, refreshTrigger) { allTracks.filter { isDownloaded(it) } }
    val filteredTracks = remember(selectedCategory, visibleTracks) {
        if (selectedCategory == "All") visibleTracks else visibleTracks.filter { it.category == selectedCategory }
    }

    // --- PLAYBACK LOGIC ---
    
    // 1. Sync the player's queue with all downloaded tracks (only when the list changes)
    LaunchedEffect(mediaController, visibleTracks) {
        val controller = mediaController ?: return@LaunchedEffect
        val currentMediaIds = (0 until controller.mediaItemCount).map { controller.getMediaItemAt(it).mediaId }
        val newMediaIds = visibleTracks.map { it.id.toString() }
        
        if (currentMediaIds != newMediaIds) {
            val playlist = visibleTracks.map { t ->
                val uri = if (t.url.isNotBlank()) {
                    val f = File(File(context.filesDir, "music"), getFileName(t))
                    if (f.exists() && f.length() > 0) f.toURI().toString() else t.url
                } else {
                    "asset:///music/${t.fileName}"
                }
                // CHANGE: Added metadata so the background notification shows the song title and artist
                val metadata = MediaMetadata.Builder()
                    .setTitle(t.title)
                    .setArtist(t.artist)
                    .setDisplayTitle(t.title)
                    .build()
                MediaItem.Builder()
                    .setUri(uri)
                    .setMediaId(t.id.toString())
                    .setMediaMetadata(metadata)
                    .build()
            }
            controller.setMediaItems(playlist)
            controller.repeatMode = Player.REPEAT_MODE_ALL // Ensure cycle buttons are always visible
            controller.prepare()
            
            // Keep the current song's place if it's still in the list
            val index = visibleTracks.indexOfFirst { it.id.toString() == currentTrack?.id.toString() }
            if (index != -1) {
                controller.seekTo(index, controller.currentPosition)
            }
        }
    }

    // 2. Consolidated Control: Handles Track Changes AND Play/Pause state together
    LaunchedEffect(currentTrack, isPlaying, mediaController) {
        val controller = mediaController ?: return@LaunchedEffect
        
        // A. Handle Track Change (Skip to the correct index)
        currentTrack?.let { track ->
            if (controller.currentMediaItem?.mediaId != track.id.toString()) {
                val index = visibleTracks.indexOfFirst { it.id.toString() == track.id.toString() }
                if (index != -1) {
                    controller.seekTo(index, 0L)
                    controller.prepare() // Ensure it's ready
                }
            }
        }
        
        // B. Handle Play/Pause - Use a slight delay or force to ensure it sticks
        if (isPlaying) {
            controller.play()
        } else {
            controller.pause()
        }
    }

    // --- LOADING THE LIST ---
    // Reads your 'music_list.json' file and turns it into the list you see on screen.
    LaunchedEffect(refreshTrigger) {
        try {
            val jsonString = context.assets.open("music_list.json").bufferedReader().use { it.readText() }
            val loadedTracks: List<Track> = Gson().fromJson(jsonString, object : TypeToken<List<Track>>() {}.type)
            allTracks.clear()
            allTracks.addAll(loadedTracks)
            
            if (currentTrack == null || !isDownloaded(currentTrack!!)) {
                currentTrack = allTracks.firstOrNull { isDownloaded(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    // -----------------------------------------------------------------------------------------
    // --- UI DESIGN SECTION (This is where you change how things LOOK) ---
    // -----------------------------------------------------------------------------------------
    
    Column(modifier = Modifier.padding(24.dp)) {
        
        // --- THE HEADER (TOP ROW) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HOW TO CHANGE: Change the text inside " " to rename your screen.
            Text("Focus Music", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            
            // This is the little Download icon button.
            IconButton(onClick = { navController.navigate(Screen.OnlineMusic.route) }) {
                Icon(Icons.Default.Download, "Online Library")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp)) // Adds a small gap
        
        // --- THE NOW PLAYING CARD (THE PURPLE BOX) ---
        Card(
            // HOW TO CHANGE SIZE: Change '150.dp' to make the box taller or shorter.
            modifier = Modifier.fillMaxWidth().height(150.dp), 
            
            // HOW TO CHANGE CORNERS: Change '20.dp' to make corners more or less round.
            shape = RoundedCornerShape(20.dp), 
            
            // HOW TO CHANGE COLOR: Change '0xFF5856D6' to another hex color code.
            // (e.g., 0xFF000000 is Black, 0xFFFF0000 is Red)
            colors = CardDefaults.cardColors(containerColor = Color(0xFF5856D6)) 
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Displays the title of the song.
                    Text("Now Playing: ${currentTrack?.title ?: "Select a track"}", color = Color.White, fontWeight = FontWeight.Bold)
                    
                    // --- PLAYER CONTROLS (Skip, Play/Pause, Next) ---
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        
                        // SKIP PREVIOUS BUTTON (Cycle Style)
                        IconButton(onClick = {
                            val list = if (filteredTracks.isNotEmpty()) filteredTracks else visibleTracks
                            if (list.isNotEmpty()) {
                                val currentId = currentTrack?.id
                                val idx = list.indexOfFirst { it.id == currentId }
                                // If at start or not found, go to last
                                val prevIdx = if (idx <= 0) list.size - 1 else idx - 1
                                currentTrack = list[prevIdx]
                                isPlaying = true
                            }
                        }) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White) }
                        
                        // PLAY / PAUSE BUTTON
                        IconButton(onClick = { isPlaying = !isPlaying }, modifier = Modifier.size(64.dp)) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                                null, 
                                tint = Color.White, 
                                modifier = Modifier.size(48.dp) 
                            ) 
                        }
                        
                        // SKIP NEXT BUTTON (Cycle Style)
                        IconButton(onClick = {
                            val list = if (filteredTracks.isNotEmpty()) filteredTracks else visibleTracks
                            if (list.isNotEmpty()) {
                                val currentId = currentTrack?.id
                                val idx = list.indexOfFirst { it.id == currentId }
                                // If at end or not found, go to first
                                val nextIdx = if (idx == -1 || idx >= list.size - 1) 0 else idx + 1
                                currentTrack = list[nextIdx]
                                isPlaying = true
                            }
                        }) { Icon(Icons.Default.SkipNext, null, tint = Color.White) }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // --- CATEGORY ROW (THE FILTER TABS) ---
        // 'LazyRow' means it scrolls left and right.
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                // Individual Tab Chip
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        // --- SONG LIST AREA ---
        if (visibleTracks.isEmpty()) {
            // This shows if you have NO music downloaded.
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No local tracks found.", color = Color.Gray)
                    Button(onClick = { navController.navigate(Screen.OnlineMusic.route) }) {
                        Text("Download from Online Library")
                    }
                }
            }
        } else {
            // 'LazyColumn' is a list that scrolls up and down.
            LazyColumn {
                items(filteredTracks) { track ->
                    // Individual Music Item in the list
                    ListItem(
                        headlineContent = { Text(track.title) }, // Main title
                        supportingContent = { Text(track.artist) }, // Small artist name
                        trailingContent = { 
                            // Small play button on the right side of the list
                            IconButton(onClick = {
                                if (currentTrack == track) isPlaying = !isPlaying else { currentTrack = track; isPlaying = true }
                            }) {
                                Icon(if (currentTrack == track && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                            }
                        }
                    )
                }
            }
        }
    }
}
