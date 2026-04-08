package com.example.myapplication.pages

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.Todo
import com.example.myapplication.TaskViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardOptions

/**
 * TodoScreen: A simple page to manage your daily tasks.
 * You can add tasks, check them off, or delete them.
 */
@Composable
fun TodoScreen(viewModel: TaskViewModel, onNavigateToMoney: () -> Unit) {
    // State to hold the text currently typed in the input box
    var text by remember { mutableStateOf("") }
    
    // Automatically updates the list whenever a task is added or removed in the ViewModel
    val todos by viewModel.todos.collectAsState()

    val moneyLimit by viewModel.moneyLimit.collectAsState()
    val moneySpent by viewModel.moneySpent.collectAsState()
    var showMoneyDialog by remember { mutableStateOf(false) }

    // Trigger for the falling "fine" animation when a task is completed
    var animationTrigger by remember { mutableStateOf(0) }
    
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background) // Explicit background color
    ) {
        // Main layout container with padding around the edges
        Column(modifier = Modifier.padding(24.dp)) {
            
            // PAGE TITLE with Money Tracker Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "To-Do List", 
                    style = MaterialTheme.typography.headlineLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                IconButton(onClick = onNavigateToMoney) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go to Money Tracker",
                        tint = if (moneyLimit > 0 && moneySpent >= moneyLimit) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // INPUT AREA: Where you type new tasks
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The text field where you type
                TextField(
                    value = text, // Shows what you've typed
                    onValueChange = { text = it }, // Updates as you type
                    modifier = Modifier.weight(1f), // Takes up all available horizontal space
                    placeholder = { Text("What needs to be done?") }, // Hint text when empty
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ), 
                    singleLine = true // Keeps input on one line
                )
                
                // The (+) button to add the task
                IconButton(onClick = { 
                    if (text.isNotBlank()) { // Only add if the text isn't empty
                        viewModel.addTodo(text) 
                        text = "" // Clears the input box after adding
                    }
                }) {
                    Icon(
                        Icons.Default.AddCircle, 
                        contentDescription = "Add", 
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // THE LIST: Displays all your tasks
            // Sorted to show incomplete tasks first, then completed ones
            val sortedTodos = remember(todos) {
                todos.sortedBy { it.isCompleted }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sortedTodos, key = { it.id }) { todo ->
                    // Renders each individual task item
                    TodoItem(
                        todo = todo, 
                        onToggle = { 
                            // If we are marking it as done (from false to true), trigger animation
                            if (!todo.isCompleted) {
                                animationTrigger++
                            }
                            viewModel.toggleTodo(todo.id) 
                        }, // When checkbox clicked
                        onDelete = { viewModel.removeTodo(todo.id) }  // When delete clicked
                    )
                }
            }
        }

        // The falling animation layer that triggers on completion
        FallingAnimation(trigger = animationTrigger)

        if (showMoneyDialog) {
            MoneyTrackerDialog(
                currentLimit = moneyLimit,
                currentSpent = moneySpent,
                onDismiss = { showMoneyDialog = false },
                onSave = { limit, spent ->
                    viewModel.setMoneyLimit(limit)
                    viewModel.setMoneySpent(spent)
                    showMoneyDialog = false
                }
            )
        }
    }
}

@Composable
fun MoneyTrackerDialog(
    currentLimit: Float,
    currentSpent: Float,
    onDismiss: () -> Unit,
    onSave: (Float, Float) -> Unit
) {
    var limitStr by remember { mutableStateOf(currentLimit.toString()) }
    var spentStr by remember { mutableStateOf(currentSpent.toString()) }
    val limit = limitStr.toFloatOrNull() ?: 0f
    val spent = spentStr.toFloatOrNull() ?: 0f
    val isOverLimit = limit > 0 && spent >= limit

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Money Spending Tracker") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isOverLimit) {
                    Text(
                        "WARNING: You have reached or exceeded your limit!",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Set Spending Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = spentStr,
                    onValueChange = { spentStr = it },
                    label = { Text("Amount Spent") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Balance: ${limit - spent}", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(limit, spent) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * FallingAnimation: A festive effect that throws down small colored bits (confetti) 
 * from the top of the screen.
 */
@Composable
fun FallingAnimation(trigger: Int) {
    if (trigger == 0) return
    
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Generate a set of festive colors for the confetti
    val festiveColors = listOf(
        Color(0xFFF44336), // Red
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFF00BCD4)  // Cyan
    )

    val particles = remember(trigger) {
        List(30) {
            ParticleData(
                xOffset = (0..100).random().toFloat() / 100f,
                duration = (1500..2500).random(),
                delay = (0..500).random(),
                color = festiveColors.random(), // Pick a random bright color
                size = (6..12).random().dp,
                rotation = (0..360).random().toFloat()
            )
        }
    }

    particles.forEach { particle ->
        val progress = remember(trigger, particle) { Animatable(0f) }
        
        LaunchedEffect(trigger, particle) {
            delay(particle.delay.toLong())
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = particle.duration, easing = LinearEasing)
            )
        }

        if (progress.value > 0f && progress.value < 1f) {
            Box(
                modifier = Modifier
                    .offset(
                        x = screenWidth * particle.xOffset,
                        y = screenHeight * progress.value - 20.dp
                    )
                    .size(particle.size)
                    .graphicsLayer(rotationZ = particle.rotation + progress.value * 720)
                    .background(particle.color, RoundedCornerShape(2.dp))
            )
        }
    }
}

/**
 * Data class representing a single falling particle.
 */
private data class ParticleData(
    val xOffset: Float,
    val duration: Int,
    val delay: Int,
    val color: Color,
    val size: androidx.compose.ui.unit.Dp,
    val rotation: Float
)

/**
 * TodoItem: Represents a single row in your task list.
 */
@Composable
fun TodoItem(todo: Todo, onToggle: () -> Unit, onDelete: () -> Unit) {
    // Animate the background color change using theme colors
    val backgroundColor by animateColorAsState(
        targetValue = if (todo.isCompleted) {
            // A theme-aware container color for completed tasks
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            // Default background for incomplete tasks
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 500),
        label = "backgroundColor"
    )

    // The container for one single task row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) 
            .background(backgroundColor)
            .padding(vertical = 4.dp, horizontal = 12.dp), // Thinner height
        verticalAlignment = Alignment.CenterVertically
    ) {
        // CHECKBOX: Click to mark as done
        Checkbox(
            checked = todo.isCompleted, 
            onCheckedChange = { onToggle() }
        )
        
        // TASK TEXT
        Text(
            text = todo.text,
            modifier = Modifier.weight(1f), 
            style = if (todo.isCompleted) {
                // If done, show with a line through the middle
                MaterialTheme.typography.titleMedium.copy( // Bigger text
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                MaterialTheme.typography.titleMedium // Bigger text
            }
        )
        
        // DELETE BUTTON
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete, 
                contentDescription = "Delete", 
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
