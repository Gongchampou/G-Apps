// LINE 1: This is the package name. It tells the app that this file belongs in the 'pages' folder.
package com.gongchampou.gapps.pages

// LINE 4: We bring in 'clickable' so we can make parts of the screen respond when you touch them.
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
// LINE 8: This makes the corners of our boxes look soft and rounded instead of pointy.
import androidx.compose.foundation.shape.RoundedCornerShape
// LINE 10: This gives us access to all the small icons like the Gear, Checkmark, and Arrow.
import androidx.compose.material.icons.Icons
// LINE 12: We import all the 'filled' icons so we can use them anywhere in the file.
import androidx.compose.material.icons.filled.*
// LINE 14: Material3 is the design language that makes the buttons and text look modern and clean.
import androidx.compose.material3.*
// LINE 16: 'runtime' tools help the app "remember" settings even when the screen changes.
import androidx.compose.runtime.*
// LINE 18: 'Alignment' is used to center text and icons so they look perfectly straight.
import androidx.compose.ui.Alignment
// LINE 20: 'Modifier' is a tool that lets us change the size, padding, and color of anything.
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
// LINE 22: 'Color' allows us to use specific shades like Green, Gray, and Blue.
import androidx.compose.ui.graphics.Color
// LINE 24: 'LocalContext' helps the app find files that are saved on your phone.
import androidx.compose.ui.platform.LocalContext
// LINE 26: 'LocalUriHandler' lets the app open web links (like GitHub) in your browser.
import androidx.compose.ui.platform.LocalUriHandler
// LINE 28: 'FontWeight' is used to make text Bold or extra thick so it's easy to read.
import androidx.compose.ui.text.font.FontWeight
// LINE 30: 'dp' is a unit for measuring distance and size on the screen.
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// LINE 32: 'NavController' is the tool that lets the user jump from one screen to another.
import androidx.navigation.NavController
// LINE 34: 'Screen' is a list of all the different pages in our app.
import com.gongchampou.gapps.Screen
// LINE 36: 'TaskViewModel' is the "Brain" that stores your settings and data.
import com.gongchampou.gapps.TaskViewModel
// LINE 38: 'Track' is a template for a single music song (Title, Artist, etc.).
import com.gongchampou.gapps.Track
// LINE 40: 'Gson' is a tool that reads the 'music_list.json' file so we know what songs exist.
import com.google.gson.Gson
// LINE 42: 'TypeToken' helps Gson understand how to turn text into a list of songs.
import com.google.gson.reflect.TypeToken
// LINE 44: 'java.io.File' is a tool that lets the app look at files in your phone's memory.
import java.io.File

/**
 * LINE 48: The SettingsScreen function builds the entire settings page.
 * It's where you change things like Dark Mode, Sound, and Font Size.
 */
@Composable
fun SettingsScreen(viewModel: TaskViewModel, navController: NavController) {
    // LINE 53: We "watch" the Dark Mode setting. If it's on, the screen turns dark.
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    // LINE 55: We "watch" the Notifications setting to see if you want alerts.
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    // LINE 57: This checks if the phone should vibrate when the timer stops.
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
    // LINE 59: This checks if the app should play a sound when the timer stops.
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    // LINE 61: This checks if the screen should stay awake when the timer is running.
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    
    // LINE 64: This tool handles opening websites for us.
    val uriHandler = LocalUriHandler.current
    // LINE 64: This tool helps find where the app stores its music files.
    val context = LocalContext.current
    
    // LINE 67: This keeps a count of how many songs you've downloaded so far.
    var downloadCount by remember { mutableIntStateOf(0) }
    
    // LINE 70: This helper function makes sure the file names match (e.g., 'track_1.mp3').
    fun getFileName(track: Track): String {
        return if (track.url.isBlank()) {
            track.fileName
        } else {
            "track_${track.id}.mp3"
        }
    }

    /**
     * LINE 80: This function looks in the 'music' folder on your phone and counts
     * only the songs that have been successfully downloaded.
     */
    fun updateDownloadCount() {
        try {
            val jsonString = context.assets.open("music_list.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Track>>() {}.type
            val allTracks: List<Track> = Gson().fromJson(jsonString, type)
            val musicDir = File(context.filesDir, "music")
            downloadCount = allTracks.count { track ->
                val file = File(musicDir, getFileName(track))
                track.url.isNotBlank() && file.exists() && file.length() > 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // LINE 98: As soon as you open this page, the app counts your downloaded songs.
    LaunchedEffect(Unit) {
        updateDownloadCount()
    }
    
    // LINE 103: Column puts all the settings in a vertical list from top to bottom.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        // LINE 106: The main title at the top of the settings page.
        Text("General Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))

        SettingsToggle("Dark Mode", isDarkMode) { viewModel.setDarkMode(it) }
        SettingsToggle("Notifications", isNotificationsEnabled) { viewModel.setNotifications(it) }
        SettingsToggle("Vibration", isVibrationEnabled) { viewModel.setVibration(it) }
        SettingsToggle("Sound", isSoundEnabled) { viewModel.setSound(it) }
        SettingsToggle("Screen Awake", keepScreenAwake) { viewModel.setKeepScreenAwake(it) }

        // LINE 116: This setting controls how you see your money tracking (Circle vs Card).
        val showCircularProgress by viewModel.showCircularProgress.collectAsState()
        SettingsToggle(
            label = if (showCircularProgress) "Money: Circle View" else "Money: Card View",
            checked = showCircularProgress
        ) { viewModel.setShowCircularProgress(it) }

        // LINE 120: CURRENCY SELECTOR
        // This is a button that opens a menu to pick your money symbol ($, €, etc).
        val currentCurrency by viewModel.currency.collectAsState()
        var showCurrencyMenu by remember { mutableStateOf(false) }
        val currencies = listOf("$", "₹", "€", "£", "¥", "₩")

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Currency", style = MaterialTheme.typography.bodyLarge)
            Box {
                OutlinedButton(
                    onClick = { showCurrencyMenu = true },
                    modifier = Modifier.height(32.dp), // Reduced height
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(currentCurrency, fontSize = 14.sp)
                }
                DropdownMenu(
                    expanded = showCurrencyMenu,
                    onDismissRequest = { showCurrencyMenu = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                viewModel.setCurrency(currency)
                                showCurrencyMenu = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // LINE 158: TIMER TONE SELECTOR
        val currentTone by viewModel.timerTone.collectAsState()
        var showToneMenu by remember { mutableStateOf(false) }
        val tones = listOf("Default", "berivan.opus", "doorbell.opus", "field-ring.opus", "fieldtone.opus", "jingle-bells.opus", "liquid-glass.opus", "normal.opus", "phone-call.opus", "ringtone.opus", "ringtone-car.opus", "spring-drip.mp3", "univ.opus", "univers.opus", "universfield.opus")

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Timer Tone", style = MaterialTheme.typography.bodyLarge)
            Box {
                OutlinedButton(
                    onClick = { showToneMenu = true },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(currentTone, fontSize = 14.sp)
                }
                DropdownMenu(
                    expanded = showToneMenu,
                    onDismissRequest = { showToneMenu = false }
                ) {
                    tones.forEach { tone ->
                        DropdownMenuItem(
                            text = { Text(tone) },
                            onClick = {
                                viewModel.setTimerTone(tone)
                                showToneMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LINE 158: E-BOOK FONT SIZE SLIDER - COMPACTED
        val ebookFontSize by viewModel.ebookFontSize.collectAsState()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("E-book Font", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = ebookFontSize,
                onValueChange = { viewModel.setEbookFontSize(it) },
                valueRange = 12f..32f,
                steps = 10,
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp).height(18.dp)
            )
            Text("${ebookFontSize.toInt()}sp", fontSize = 12.sp, modifier = Modifier.width(36.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LINE 176: DOWNLOADED MUSIC FOLDER - COMPACTED
        Surface(
            onClick = { navController.navigate(Screen.DownloadedMusic.route) },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp), // Reduced padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) // Smaller icon
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Downloaded Music", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("$downloadCount tracks saved", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
            }
        }

        // LINE 200: INFO SECTION
        // This shows the app version and developer name.

        Spacer(modifier = Modifier.height(12.dp))
        Text("About", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("This app was created by Gongchampou Kamei.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))
        Text("G Apps Version: 1.0.0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // LINE 212: THE GITHUB BUTTON
        // This opens the project code on GitHub so anyone can see it.
        Button(
            onClick = {
                uriHandler.openUri("https://github.com/Gongchampou/an-focus.git")
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("View on GitHub", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * SettingsToggle is a special helper row for settings like 'Dark Mode'.
 */
@Composable
fun SettingsToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)

        // I put the switch back but scaled it down to 0.7x size to make it look smaller.
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.7f)
        )
    }
}
