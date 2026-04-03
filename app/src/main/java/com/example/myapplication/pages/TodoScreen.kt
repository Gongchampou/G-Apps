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

@Composable
fun TodoScreen(viewModel: TaskViewModel) {
    var text by remember { mutableStateOf("") }
    val todos by viewModel.todos.collectAsState()
    
    Column(modifier = Modifier.padding(24.dp)) {
        Text("To-Do List", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        
        Row(modifier = Modifier.padding(vertical = 16.dp)) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("What needs to be done?") },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent)
            )
            IconButton(onClick = { viewModel.addTodo(text); text = "" }) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = Color(0xFF007AFF))
            }
        }
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(todos, key = { it.id }) { todo ->
                TodoItem(todo, 
                    onToggle = { viewModel.toggleTodo(todo.id) },
                    onDelete = { viewModel.removeTodo(todo.id) }
                )
            }
        }
    }
}

@Composable
fun TodoItem(todo: Todo, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = todo.isCompleted, onCheckedChange = { onToggle() })
        Text(
            text = todo.text,
            modifier = Modifier.weight(1f),
            style = if (todo.isCompleted) MaterialTheme.typography.bodyLarge.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyLarge
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30))
        }
    }
}
