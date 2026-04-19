package com.gongchampou.gapps.pages

import android.content.ComponentName
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.gongchampou.gapps.PlaybackService
import com.gongchampou.gapps.Track
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
import com.gongchampou.gapps.Screen

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
    val categories = listOf("All", "Study", "Sleep", "Relaxation", "Work", "Focus")
    
    /**
     * --- BACKGROUND CUSTOMIZATION GUIDE ---
     * Change the values below to update the "Now Playing" background for each category:
     * 1. SVG Code: Keep the """<svg>...</svg>""" format.
     * 2. Local Image (PNG/JPG/GIF/AVIF): Use "file:///android_asset/music-play/your_image.png"
     * 3. Web Image: Use "https://example.com/image.jpg"
     * 
     * Note: GIFs will animate automatically!
     */
    val categoryBackgrounds = remember {
        mutableStateMapOf(
            "Study" to "file:///android_asset/images/music-play/beach.gif/",
            "Sleep" to "file:///android_asset/images/music-play/sleepwork.gif/",
            "Relaxation" to "file:///android_asset/images/music-play/relax.gif/",
            "Work" to "file:///android_asset/images/music-play/work.gif/",
            "Focus" to "file:///android_asset/images/music-play/focus.gif/"
        )
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
    
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

    // This part handles the lifecycle. We've removed the auto-refresh on resume
    // to keep the UI consistent and prevent flickering.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ -> }
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
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.containsAny(Player.EVENT_PLAY_WHEN_READY_CHANGED, Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                    isPlaying = player.playWhenReady
                    val mediaId = player.currentMediaItem?.mediaId
                    if (mediaId != null) {
                        val track = allTracks.find { it.id.toString() == mediaId }
                        if (track != null) currentTrack = track
                    }
                }
            }
        }
        controller.addListener(listener)
        isPlaying = controller.playWhenReady
        controller.currentMediaItem?.mediaId?.let { id ->
            currentTrack = allTracks.find { it.id.toString() == id }
        }
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
    // We use the IDs of the tracks to detect actual changes and avoid unnecessary refreshes.
    val trackIds = remember(visibleTracks) { visibleTracks.map { it.id } }
    LaunchedEffect(mediaController, trackIds) {
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
            
            // SMART SYNC: Update the list but stay on the same song at the same second
            val currentId = controller.currentMediaItem?.mediaId
            val currentIndex = visibleTracks.indexOfFirst { it.id.toString() == currentId }
            
            if (currentIndex != -1) {
                controller.setMediaItems(playlist, currentIndex, controller.currentPosition)
            } else {
                controller.setMediaItems(playlist)
            }
            
            controller.repeatMode = Player.REPEAT_MODE_ALL
            controller.prepare()
        }
    }

    // REMOVED the old "Consolidated Control" LaunchedEffect that was causing resets.
    // Playback is now handled directly by button clicks for better stability.

    // --- LOADING THE LIST ---
    // Reads your 'music_list.json' file once when the screen starts.
    LaunchedEffect(Unit) {
        if (allTracks.isNotEmpty()) return@LaunchedEffect
        try {
            val jsonString = context.assets.open("music_list.json").bufferedReader().use { it.readText() }
            val loadedTracks: List<Track> = Gson().fromJson(jsonString, object : TypeToken<List<Track>>() {}.type)
            allTracks.addAll(loadedTracks)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Sync currentTrack with what's actually playing in the mediaController
    // This runs as soon as the controller is ready or the tracks are loaded
    LaunchedEffect(mediaController, allTracks.size) {
        val controller = mediaController ?: return@LaunchedEffect
        if (allTracks.isEmpty()) return@LaunchedEffect

        val playingMediaId = controller.currentMediaItem?.mediaId
        if (playingMediaId != null) {
            val playingTrack = allTracks.find { it.id.toString() == playingMediaId }
            if (playingTrack != null) {
                currentTrack = playingTrack
                isPlaying = controller.playWhenReady
            }
        } else if (currentTrack == null) {
            // Only set a default if nothing is already playing
            currentTrack = allTracks.firstOrNull { isDownloaded(it) }
        }
    }



    // --- UI DESIGN SECTION (This is where you change how things LOOK) ---
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
        
        // --- THE NOW PLAYING CARD (THE TRANSLUCENT BOX) ---
        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp), // Increased height for trackline
            shape = RoundedCornerShape(20.dp), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // BACKGROUND IMAGE (Follows the filter/category)
                val bgCategory = if (selectedCategory != "All") selectedCategory else currentTrack?.category
                val backgroundModel = categoryBackgrounds[bgCategory]

                if (backgroundModel != null) {
                    val isRawSvg = backgroundModel.startsWith("<svg")
                    val painter = rememberAsyncImagePainter(
                        model = if (isRawSvg) backgroundModel.toByteArray() else backgroundModel,
                        imageLoader = imageLoader
                    )
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Now Playing: ${currentTrack?.title ?: "Select a track"}", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isPlaying) {
                            Spacer(Modifier.width(8.dp))
                            PlayingAnimation()
                        }
                    }

                    // --- PLAYER CONTROLS (Above tracking now) ---
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        IconButton(onClick = { mediaController?.seekToPreviousMediaItem() }) { 
                            Icon(Icons.Default.SkipPrevious, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) 
                        }
                        
                        IconButton(onClick = { 
                            val controller = mediaController ?: return@IconButton
                            if (controller.playWhenReady) controller.pause() else controller.play()
                        }, modifier = Modifier.size(56.dp)) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                                null, 
                                tint = MaterialTheme.colorScheme.primary, 
                                modifier = Modifier.size(40.dp) 
                            ) 
                        }
                        
                        IconButton(onClick = { mediaController?.seekToNextMediaItem() }) { 
                            Icon(Icons.Default.SkipNext, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) 
                        }
                    }

                    // --- THE TRACKLINE (SLIDER) ---
                    if (currentTrack != null) {
                        PlaybackProgress(mediaController, isPlaying)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // --- CATEGORY ROW (THE FILTER TABS) ---
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories, key = { it }) { category ->
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
            LazyColumn {
                items(filteredTracks, key = { it.id }) { track ->
                    // Individual Music Item in the list
                    ListItem(
                        headlineContent = { Text(track.title) },
                        supportingContent = { Text(track.artist) },
                        trailingContent = { 
                            IconButton(onClick = {
                                val controller = mediaController ?: return@IconButton
                                if (currentTrack?.id == track.id) {
                                    if (isPlaying) controller.pause() else controller.play()
                                } else {
                                    val index = visibleTracks.indexOfFirst { it.id == track.id }
                                    if (index != -1) {
                                        controller.seekTo(index, 0L)
                                        controller.play()
                                    }
                                }
                            }) {
                                Icon(if (currentTrack?.id == track.id && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * A small animated visualizer that shows when music is playing.
 */
@Composable
fun PlayingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "bars")
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(16.dp)
    ) {
        listOf(0.4f, 0.8f, 0.5f).forEach { initialHeight ->
            val height by infiniteTransition.animateFloat(
                initialValue = initialHeight,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(height)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
            )
        }
    }
}

/**
 * A self-contained progress bar to prevent flickering on the main screen.
 */
@Composable
fun PlaybackProgress(mediaController: MediaController?, isPlaying: Boolean) {
    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    LaunchedEffect(mediaController, isPlaying) {
        val controller = mediaController ?: return@LaunchedEffect
        while (isPlaying) {
            currentPosition = controller.currentPosition
            totalDuration = controller.duration.coerceAtLeast(0L)
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Slider(
            value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f,
            onValueChange = { ratio ->
                val newPos = (ratio * totalDuration).toLong()
                currentPosition = newPos
                mediaController?.seekTo(newPos)
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            ),
            modifier = Modifier.height(20.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(currentPosition), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatTime(totalDuration), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
