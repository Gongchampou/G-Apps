package com.example.myapplication.pages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.Task
import com.example.myapplication.TaskViewModel
import com.example.myapplication.formatDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Main screen for the Focus Timer functionality.
 * Handles task creation, listing, and navigation to the active timer.
 */
@Composable
fun TimerScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val characterImages by viewModel.characters.collectAsState()
    
    // Tracks which task is currently being focused on
    var focusedTaskId by remember { mutableStateOf<java.util.UUID?>(null) }
    
    // State for the "Add Task" input fields
    var newTaskName by remember { mutableStateOf("") }
    var selectedHours by remember { mutableIntStateOf(0) }
    var selectedMinutes by remember { mutableIntStateOf(0) }
    var selectedSeconds by remember { mutableIntStateOf(0) }
    var selectedImagePath by remember { mutableStateOf("") }
    
    // Initialize default character image
    LaunchedEffect(characterImages) {
        if (selectedImagePath.isEmpty() && characterImages.isNotEmpty()) {
            selectedImagePath = characterImages.first().imagePath
        }
    }
    
    // Automatically switch to active timer view if a task is already running
    LaunchedEffect(tasks) {
        tasks.find { it.isRunning }?.let {
            focusedTaskId = it.id
        }
    }

    val focusedTask = tasks.find { it.id == focusedTaskId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (focusedTask != null) {
            // Display the active circular countdown
            ActiveFocusDisplay(
                task = focusedTask, 
                onStop = {
                    if (focusedTask.isRunning) {
                        viewModel.toggleTask(focusedTask.id)
                    }
                    focusedTaskId = null
                },
                viewModel = viewModel
            )
        } else {
            // Main List View & Creator
            Text("Focus Timer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            // --- Task Creator Card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Task Name Input
                    TextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("What are you focusing on?", fontSize = 16.sp) },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                        colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // H:M:S Scrolling Pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeValuePicker(label = "H", value = selectedHours, range = 0..23, onValueChange = { selectedHours = it })
                        Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))
                        TimeValuePicker(label = "M", value = selectedMinutes, range = 0..59, onValueChange = { selectedMinutes = it })
                        Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))
                        TimeValuePicker(label = "S", value = selectedSeconds, range = 0..59, onValueChange = { selectedSeconds = it })
                    }

                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Character Avatar Selector
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(characterImages) { char ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedImagePath == char.imagePath) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                        .border(
                                            width = 2.dp,
                                            color = if (selectedImagePath == char.imagePath) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f),
                                            shape = CircleShape
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
                        
                        // Add Task Button (+)
                        IconButton(
                            onClick = { 
                                if (newTaskName.isNotBlank()) {
                                    viewModel.addTask(
                                        newTaskName, 
                                        selectedHours,
                                        selectedMinutes,
                                        selectedSeconds,
                                        selectedImagePath
                                    )
                                    newTaskName = "" // Reset form
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = "Add Task", 
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // --- List of Inactive Tasks ---
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(tasks, key = { it.id }) { task ->
                    if (!task.isRunning) {
                        TaskCard(
                            task = task, 
                            onToggle = { viewModel.toggleTask(task.id) },
                            onDelete = { viewModel.removeTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-screen display for an active timer.
 * Features a circular progress bar and a breathing character animation.
 */
@Composable
fun ActiveFocusDisplay(task: Task, onStop: () -> Unit, viewModel: TaskViewModel) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Update current time every second to refresh UI
    LaunchedEffect(task.id) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    // Calculate how much time is left based on last start time
    val displayRemaining = if (task.lastStartTime != null) {
        val elapsed = currentTimeMillis - task.lastStartTime
        (task.remainingTimeMillis - elapsed).coerceAtLeast(0L)
    } else {
        task.remainingTimeMillis
    }

    val isFinished = displayRemaining <= 0 && !task.isRunning

    // Handle vibration and sound repetition when the timer hits zero
    LaunchedEffect(isFinished) {
        if (isFinished) {
            while (true) {
                viewModel.settingsManager.vibrationFlow.first().let { if (it) viewModel.vibrate() }
                viewModel.settingsManager.soundEffectsFlow.first().let { if (it) viewModel.playSound() }
                delay(2000) // Repeated alert
            }
        }
    }

    // Automatically stop task in ViewModel when time expires
    LaunchedEffect(displayRemaining) {
        if (displayRemaining <= 0 && task.isRunning) {
            viewModel.toggleTask(task.id)
        }
    }

    val progress = if (task.initialTimeMillis > 0) {
        displayRemaining.toFloat() / task.initialTimeMillis.toFloat()
    } else 0f

    // Smooth animation for the circular progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "TimerProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp)
        ) {
            val progressColor = if (isFinished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

            // Timer Circle
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                // Outer Track
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = strokeWidth)
                )
                // Active Progress Arc
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Central Character Image
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // "Breathing" scale effect when time is up
                val scale by animateFloatAsState(
                    targetValue = if (isFinished) 1.2f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "BreatheAnimation"
                )

                Image(
                    painter = rememberAsyncImagePainter("file:///android_asset/images/${task.characterImageName}"),
                    contentDescription = null,
                    modifier = Modifier.size(180.dp).scale(if (isFinished) scale else 1f),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Countdown Text
        Text(
            text = if (isFinished) "TIME'S UP!" else formatDigitalClock(displayRemaining),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = if (isFinished) 48.sp else 72.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFinished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        )

        Text(
            text = task.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(if (isFinished) 32.dp else 48.dp))

        // Show the original set time only when finished
        if (isFinished) {
            Text(
                text = formatDigitalClock(task.initialTimeMillis),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Action Button: Stop or Finish
        Button(
            onClick = onStop,
            modifier = Modifier
                .height(64.dp)
                .width(240.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFinished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            ),
            shape = CircleShape
        ) {
            Icon(
                if (isFinished) Icons.Default.Check else Icons.Default.Stop, 
                contentDescription = null, 
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(if (isFinished) "Finished" else "Stop Focus", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * A card representing a single task in the list.
 */
@Composable
fun TaskCard(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    val displayRemaining = task.remainingTimeMillis

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                // Double tap to start focusing
                detectTapGestures(
                    onDoubleTap = { onToggle() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter("file:///android_asset/images/${task.characterImageName}"),
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                // Original set duration shown under title
                Text(
                    text = formatDigitalClock(task.initialTimeMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Current remaining time
            Text(
                text = formatDigitalClock(displayRemaining),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Converts milliseconds to HH:MM:SS string format.
 */
fun formatDigitalClock(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * A custom vertical scrolling picker for time units (H, M, or S).
 */
@Composable
fun TimeValuePicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = value)

    // Center-snapping logic for the scrolling picker
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closestItem = visibleItems.minByOrNull { 
                    Math.abs((it.offset + it.size / 2) - viewportCenter) 
                }
                closestItem?.let {
                    val newValue = it.index.coerceIn(range)
                    onValueChange(newValue)
                    listState.animateScrollToItem(it.index)
                }
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(80.dp)
                .width(42.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                items(range.last - range.first + 1) { index ->
                    val i = range.first + index
                    val isSelected = i == value
                    Text(
                        text = String.format("%02d", i),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = if (isSelected) 24.sp else 16.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clickable { 
                                onValueChange(i)
                            }
                    )
                }
            }
            // Visual guides for the selection area
            HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter).width(30.dp).padding(top = 28.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter).width(30.dp).padding(bottom = 28.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 2.dp))
    }
}
