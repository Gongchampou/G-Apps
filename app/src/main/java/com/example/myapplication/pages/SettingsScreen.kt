package com.example.myapplication.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.Screen
import com.example.myapplication.TaskViewModel
import com.example.myapplication.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * SettingsScreen: Manages application-wide preferences and storage overview.
 * Users can toggle theme modes, control notifications, and access the storage management view.
 */
@Composable
fun SettingsScreen(viewModel: TaskViewModel, navController: NavController) {
    // INSTRUCTION: Observes ViewModel state for persistent user preferences
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    
    // INSTRUCTION: Tool for opening external links (GitHub)
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    
    // INSTRUCTION: Reactive count of successfully downloaded tracks
    var downloadCount by remember { mutableIntStateOf(0) }
    
    // INSTRUCTION: Utility to match the track's local storage identity
    fun getFileName(track: Track): String {
        return if (track.url.isBlank()) {
            track.fileName
        } else {
            "track_${track.id}.mp3"
        }
    }

    /**
     * updateDownloadCount: Verifies storage state and updates the local count.
     * Only counts tracks that physically exist and are not corrupted (empty).
     */
    fun updateDownloadCount() {
        try {
            val jsonString = context.assets.open("music_list.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Track>>() {}.type
            val allTracks: List<Track> = Gson().fromJson(jsonString, type)
            val musicDir = File(context.filesDir, "music")
            // INSTRUCTION: Filter logic matches DownloadedMusicScreen for total consistency
            downloadCount = allTracks.count { track ->
                val file = File(musicDir, getFileName(track))
                track.url.isNotBlank() && file.exists() && file.length() > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // INSTRUCTION: Recalculate count whenever this screen is displayed
    LaunchedEffect(Unit) {
        updateDownloadCount()
    }
    
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        // INSTRUCTION: Toggle controls for basic app settings
        SettingsToggle("Dark Mode", isDarkMode) { viewModel.setDarkMode(it) }
        SettingsToggle("Enable Notifications", isNotificationsEnabled) { viewModel.setNotifications(it) }
        SettingsToggle("Vibration on Stop", isVibrationEnabled) { viewModel.setVibration(it) }
        SettingsToggle("Sound on Stop", isSoundEnabled) { viewModel.setSound(it) }

        Spacer(modifier = Modifier.height(24.dp))
        
        // INSTRUCTION: E-book Font Size Control
        Text("E-book Font Size", fontWeight = FontWeight.Bold)
        val ebookFontSize by viewModel.ebookFontSize.collectAsState()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${ebookFontSize.toInt()} sp", modifier = Modifier.width(48.dp))
            Slider(
                value = ebookFontSize,
                onValueChange = { viewModel.setEbookFontSize(it) },
                valueRange = 12f..32f,
                steps = 10,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // INSTRUCTION: Entry point to Downloaded Music management
        Surface(
            onClick = { navController.navigate(Screen.DownloadedMusic.route) },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Downloaded Music", fontWeight = FontWeight.Bold)
                    Text("$downloadCount tracks saved", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // INSTRUCTION: App Information and Branding section
        Text("Open Source Focus App v1.0", color = Color.Gray, fontWeight = FontWeight.Bold)
        Text("Completely free and private. No accounts needed.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))
        Text("About", color = Color.Gray, fontWeight = FontWeight.Bold)
        Text("This app is Created by Gongchampou kamei.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))
        // INSTRUCTION: External link to view source code
        Button(
            onClick = { 
                uriHandler.openUri("https://github.com/Gongchampou/an-focus.git")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("View on GitHub", color = Color.White)
        }
    }
}

/**
 * SettingsToggle: A reusable UI component for labeled switches.
 */
@Composable
fun SettingsToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
