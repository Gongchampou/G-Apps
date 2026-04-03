package com.example.myapplication.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.Track

@Composable
fun MusicScreen() {
    val tracks = remember {
        listOf(
            Track(1, "Deep Focus", "Nature"),
            Track(2, "Ambient Study", "Lo-Fi"),
            Track(3, "Binaural Beats", "Zen"),
            Track(4, "Rainy Night", "Atmosphere"),
            Track(5, "Ocean Waves", "Nature"),
            Track(6, "Library Silence", "Study"),
            Track(7, "Alpha Waves", "Neuro"),
            Track(8, "Soft Piano", "Relax"),
            Track(9, "White Noise", "Focus"),
            Track(10, "Morning Dew", "Calm")
        )
    }
    
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Deep Learning Music", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF5856D6))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Now Playing: Deep Focus", color = Color.White, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, "", tint = Color.White) }
                        IconButton(onClick = {}, modifier = Modifier.size(64.dp)) { Icon(Icons.Default.PlayArrow, "", tint = Color.White, modifier = Modifier.size(48.dp)) }
                        IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, "", tint = Color.White) }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(tracks) { track ->
                ListItem(
                    headlineContent = { Text(track.title) },
                    supportingContent = { Text(track.artist) },
                    trailingContent = { Icon(Icons.Default.PlayArrow, null) }
                )
            }
        }
    }
}
