package com.example.myapplication.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.Todo
import com.example.myapplication.TaskViewModel

/**
 * TodoScreen: A simple page to manage your daily tasks.
 * You can add tasks, check them off, or delete them.
 */
@Composable
fun TodoScreen(viewModel: TaskViewModel) {
    // State to hold the text currently typed in the input box
    var text by remember { mutableStateOf("") }
    
    // Automatically updates the list whenever a task is added or removed in the ViewModel
    val todos by viewModel.todos.collectAsState()
    
    // Main layout container with padding around the edges
    Column(modifier = Modifier.padding(24.dp)) {
        
        // PAGE TITLE
        // CHANGE: Rename "To-Do List" to something else if you prefer
        Text(
            text = "To-Do List", 
            style = MaterialTheme.typography.headlineLarge, 
            fontWeight = FontWeight.Bold
        )
        
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
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent), // Transparent background
                singleLine = true // Keeps input on one line
            )
            
            // The (+) button to add the task
            IconButton(onClick = { 
                if (text.isNotBlank()) { // Only add if the text isn't empty
                    viewModel.addTodo(text) 
                    text = "" // Clears the input box after adding
                }
            }) {
                // CHANGE: Change the color (0xFF007AFF) to customize the add button
                Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = Color(0xFF007AFF))
            }
        }
        
        // THE LIST: Displays all your tasks
        // LazyColumn is efficient and only renders what's visible on screen
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(todos, key = { it.id }) { todo ->
                // Renders each individual task item
                TodoItem(
                    todo = todo, 
                    onToggle = { viewModel.toggleTodo(todo.id) }, // When checkbox clicked
                    onDelete = { viewModel.removeTodo(todo.id) }  // When delete clicked
                )
            }
        }
    }
}

/**
 * TodoItem: Represents a single row in your task list.
 */
@Composable
fun TodoItem(todo: Todo, onToggle: () -> Unit, onDelete: () -> Unit) {
    // The container for one single task row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // CHANGE: Adjust corner roundness of task boxes
            .background(MaterialTheme.colorScheme.surfaceVariant) // Light gray background
            .padding(12.dp),
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
            modifier = Modifier.weight(1f), // Takes up middle space
            style = if (todo.isCompleted) {
                // If done, show with a line through the middle
                MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                    color = Color.Gray
                )
            } else {
                MaterialTheme.typography.bodyLarge
            }
        )
        
        // DELETE BUTTON
        IconButton(onClick = onDelete) {
            // CHANGE: Change the color (0xFFFF3B30) to customize the delete icon
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30))
        }
    }
}
