package com.example.myapplication.pages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.Task
import com.example.myapplication.TaskViewModel
import com.example.myapplication.formatDuration
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun TimerScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val characterImages by viewModel.characters.collectAsState()
    
    var newTaskName by remember { mutableStateOf("") }
    var newTaskMinutes by remember { mutableStateOf("25") }
    var selectedImagePath by remember { mutableStateOf("") }
    
    // Set default selection when characters load
    LaunchedEffect(characterImages) {
        if (selectedImagePath.isEmpty() && characterImages.isNotEmpty()) {
            selectedImagePath = characterImages.first().imagePath
        }
    }
    
    val runningTask = tasks.find { it.isRunning }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (runningTask != null) {
            ActiveFocusDisplay(runningTask, onStop = { viewModel.toggleTask(runningTask.id) })
        } else {
            Text("Focus Timer", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            // Add Task UI
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Task Name (e.g. Study)") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Select Character:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(characterImages) { char ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedImagePath == char.imagePath) Color(0xFF007AFF).copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedImagePath == char.imagePath) Color(0xFF007AFF) else Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedImagePath = char.imagePath }
                                    .padding(4.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter("file:///android_asset/images/${char.imagePath}"),
                                    contentDescription = char.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = newTaskMinutes,
                            onValueChange = { if (it.all { char -> char.isDigit() }) newTaskMinutes = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Minutes") },
                            label = { Text("Duration (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { 
                                if (newTaskName.isNotBlank()) {
                                    viewModel.addTask(
                                        newTaskName, 
                                        newTaskMinutes.toIntOrNull() ?: 25,
                                        selectedImagePath
                                    )
                                    newTaskName = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(150.dp)
                    .background(Color.Gray.copy(alpha = 0.1f), CircleShape)
            ) {
                if (selectedImagePath.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter("file:///android_asset/images/$selectedImagePath"),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        alpha = 0.5f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(tasks, key = { it.id }) { task ->
                if (!task.isRunning) {
                    TaskCard(task, onToggle = { viewModel.toggleTask(task.id) })
                }
            }
        }
    }
}

@Composable
fun ActiveFocusDisplay(task: Task, onStop: () -> Unit) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(task.id) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val displayRemaining = if (task.lastStartTime != null) {
        val elapsed = currentTimeMillis - task.lastStartTime
        (task.remainingTimeMillis - elapsed).coerceAtLeast(0L)
    } else {
        task.remainingTimeMillis
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .size(300.dp)
                .aspectRatio(1f),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                        )
                    )
            ) {
                FocusImageAnimation(imagePath = task.characterImageName)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = formatDigitalClock(displayRemaining),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF007AFF)
            )
        )

        Text(
            text = task.name,
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onStop,
            modifier = Modifier
                .height(60.dp)
                .width(200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Focus", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FocusImageAnimation(imagePath: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "focus")
    
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Image(
        painter = rememberAsyncImagePainter("file:///android_asset/images/$imagePath"),
        contentDescription = null,
        modifier = Modifier
            .size(240.dp)
            .offset(y = bounce.dp)
            .scale(scale),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun TaskCard(task: Task, onToggle: () -> Unit) {
    val displayRemaining = task.remainingTimeMillis

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small character thumbnail in card
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter("file:///android_asset/images/${task.characterImageName}"),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${task.initialTimeMillis / (60 * 1000)} mins total",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDigitalClock(displayRemaining),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = Color(0xFF4CD964)
                    )
                }
            }
        }
    }
}

fun formatDigitalClock(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
