package com.example.myapplication.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.TaskViewModel

@Composable
fun SettingsScreen(viewModel: TaskViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val uriHandler = LocalUriHandler.current
    
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        SettingsToggle("Dark Mode", isDarkMode) { viewModel.setDarkMode(it) }
        SettingsToggle("Enable Notifications", isNotificationsEnabled) { viewModel.setNotifications(it) }
        SettingsToggle("Vibration on Stop", isVibrationEnabled) { viewModel.setVibration(it) }
        SettingsToggle("Sound on Stop", isSoundEnabled) { viewModel.setSound(it) }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Open Source Focus App v1.0", color = Color.Gray, fontWeight = FontWeight.Bold)
        Text("Completely free and private. No accounts needed.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))
        Text("About", color = Color.Gray, fontWeight = FontWeight.Bold)
        Text("This app is Created by Gongchampou kamei.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))
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
