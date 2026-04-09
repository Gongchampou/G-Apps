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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.view.WindowManager
import com.example.myapplication.Task
import com.example.myapplication.TaskViewModel
import com.example.myapplication.formatDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * TIMER SCREEN
 * This is the heart of the app! You can create focus tasks here,
 * set a timer, and watch a cute character while you work.
 */
@Composable
fun TimerScreen(viewModel: TaskViewModel) {
    // These link the screen to your "Database" (TaskViewModel)
    val tasks by viewModel.tasks.collectAsState() // All your created tasks
    val characterImages by viewModel.characters.collectAsState() // List of character images
    
    // Tracks which task is currently being focused on (active timer)
    var focusedTaskId by remember { mutableStateOf<java.util.UUID?>(null) }
    
    // --- NEW TASK FORM STATES ---
    // These store what you type/select when making a new task
    var newTaskName by remember { mutableStateOf("") }
    var selectedHours by remember { mutableIntStateOf(0) }
    var selectedMinutes by remember { mutableIntStateOf(0) }
    var selectedSeconds by remember { mutableIntStateOf(0) }
    var selectedImagePath by remember { mutableStateOf("") }
    
    // Selects the first character automatically when the app starts
    LaunchedEffect(characterImages) {
        if (selectedImagePath.isEmpty() && characterImages.isNotEmpty()) {
            selectedImagePath = characterImages.first().imagePath
        }
    }
    
    // If a timer is already running, jump straight to the "Active Timer" view
    LaunchedEffect(tasks) {
        tasks.find { it.isRunning }?.let {
            focusedTaskId = it.id
        }
    }

    // Helper: Finds the actual task object from the ID
    val focusedTask = tasks.find { it.id == focusedTaskId }

    // --- MAIN SCREEN LAYOUT ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (focusedTask != null) {
            // VIEW A: THE ACTIVE TIMER (Big circle, countdown)
            ActiveFocusDisplay(
                task = focusedTask, 
                onStop = {
                    if (focusedTask.isRunning) {
                        viewModel.toggleTask(focusedTask.id)
                    }
                    viewModel.stopSound() // Stop any playing ringtone
                    focusedTaskId = null
                },
                viewModel = viewModel
            )
        } else {
            // VIEW B: THE TASK LIST & CREATOR
            
            // TITLE
            // CHANGE: Change "Focus Timer" to rename the screen
            Text("Focus Timer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            // --- TASK CREATOR BOX ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), // CHANGE: Adjust box roundness
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Task Name Input Field
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
                    
                    // --- TIME PICKERS (H:M:S) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeValuePicker(label = "H", value = selectedHours, range = 0..23, onValueChange = { selectedHours = it })
                        Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TimeValuePicker(label = "M", value = selectedMinutes, range = 0..59, onValueChange = { selectedMinutes = it })
                        Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TimeValuePicker(label = "S", value = selectedSeconds, range = 0..59, onValueChange = { selectedSeconds = it })
                    }

                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- CHARACTER SELECTION (SCROLLS LEFT/RIGHT) ---
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
                                    // Shows character images from the 'assets/images' folder
                                    Image(
                                        painter = rememberAsyncImagePainter("file:///android_asset/images/character/${char.imagePath}"),
                                        contentDescription = char.name,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        
                        // --- ADD BUTTON (+) ---
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
                                    newTaskName = "" // Clears the name after adding
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
            
            // --- YOUR TASK LIST ---
            // Displays all the tasks you've created that aren't running
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
 * ActiveFocusDisplay: The big circle and countdown timer.
 */
@Composable
fun ActiveFocusDisplay(task: Task, onStop: () -> Unit, viewModel: TaskViewModel) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    val context = LocalContext.current

    // Keep screen awake logic
    DisposableEffect(task.isRunning, keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (task.isRunning && keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Refreshes the timer every 1 second
    LaunchedEffect(task.id) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    // MATH: Calculates how much time is left to show on the clock
    val displayRemaining = if (task.lastStartTime != null) {
        val elapsed = currentTimeMillis - task.lastStartTime
        (task.remainingTimeMillis - elapsed).coerceAtLeast(0L)
    } else {
        task.remainingTimeMillis
    }

    // Checks if the timer has reached 00:00:00
    val isFinished = displayRemaining <= 0 && !task.isRunning

    // ALARMS: Makes the phone vibrate/play sound when the timer ends
    LaunchedEffect(isFinished) {
        if (isFinished) {
            // Check settings once and play/vibrate
            val vibrationEnabled = viewModel.settingsManager.vibrationFlow.first()
            val soundEnabled = viewModel.settingsManager.soundEffectsFlow.first()

            while (true) {
                if (vibrationEnabled) viewModel.vibrate()
                if (soundEnabled) viewModel.playSound()
                delay(3000) // Repeat every 3 seconds to avoid sound overlapping too much
            }
        }
    }

    // Automatically stops the "running" state when time hits zero
    LaunchedEffect(displayRemaining) {
        if (displayRemaining <= 0 && task.isRunning) {
            viewModel.toggleTask(task.id)
        }
    }

    // Progress bar calculation (0.0 to 1.0)
    val progress = if (task.initialTimeMillis > 0) {
        displayRemaining.toFloat() / task.initialTimeMillis.toFloat()
    } else 0f

    // Smooth animation for the circle progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "TimerProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // --- THE BIG TIMER CIRCLE ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(300.dp) // CHANGE: Size of the entire circle
                .padding(16.dp)
        ) {
            // Colors for the circle
            val progressColor = if (isFinished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

            // Drawing the actual progress circle
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(
                    (size.width - diameter) / 2,
                    (size.height - diameter) / 2
                )
                val arcSize = Size(diameter, diameter)

                // Background gray circle
                drawCircle(
                    color = trackColor,
                    radius = diameter / 2,
                    style = Stroke(width = strokeWidth)
                )
                // Colored progress arc
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // --- THE CHARACTER IN THE CENTER ---
            Box(
                modifier = Modifier.size(220.dp).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // BREATHING EFFECT: Character gets slightly bigger/smaller when time is up
                val scale by animateFloatAsState(
                    targetValue = if (isFinished) 1.2f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "BreatheAnimation"
                )

                Image(
                    painter = rememberAsyncImagePainter("file:///android_asset/images/character/${task.characterImageName}"),
                    contentDescription = null,
                    modifier = Modifier.size(180.dp).scale(if (isFinished) scale else 1f),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- THE COUNTDOWN NUMBERS ---
        Text(
            text = if (isFinished) "TIME'S UP!" else formatDigitalClock(displayRemaining),
            style = MaterialTheme.typography.displayLarge.copy(
                // CHANGE: FontSize of the timer numbers
                fontSize = if (isFinished) 48.sp else 72.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFinished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        )

        // The name of the task
        Text(
            text = task.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(if (isFinished) 32.dp else 48.dp))

        // --- STOP BUTTON ---
        Button(
            onClick = onStop,
            modifier = Modifier.height(64.dp).width(240.dp),
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
            // CHANGE: Change button text labels here
            Text(if (isFinished) "Finished" else "Stop Focus", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * TaskCard: One single box in the list of tasks.
 */
@Composable
fun TaskCard(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                // TIP: Double tap the task in the list to start it!
                detectTapGestures(onDoubleTap = { onToggle() })
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small character avatar
            Image(
                painter = rememberAsyncImagePainter("file:///android_asset/images/character/${task.characterImageName}"),
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Task info
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(
                    text = "Goal: " + formatDigitalClock(task.initialTimeMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Time remaining
            Text(
                text = formatDigitalClock(task.remainingTimeMillis),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            // Delete trash can button
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * HELPER: Turns milliseconds (like 60000) into a time string (like "00:01:00")
 */
fun formatDigitalClock(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * TimeValuePicker: The scrolling numbers you use to set the time.
 */
@Composable
fun TimeValuePicker(label: String, value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = value)

    // Automatically snaps to the center number when you stop scrolling
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
                    onValueChange(it.index.coerceIn(range))
                    listState.animateScrollToItem(it.index)
                }
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.height(80.dp).width(42.dp), contentAlignment = Alignment.Center) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 24.dp)) {
                items(range.last - range.first + 1) { index ->
                    val i = range.first + index
                    val isSelected = i == value
                    Text(
                        text = String.format("%02d", i),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = if (isSelected) 24.sp else 16.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.padding(vertical = 2.dp).clickable { onValueChange(i) }
                    )
                }
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}
