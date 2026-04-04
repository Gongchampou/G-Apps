package com.example.myapplication.pages

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.Ebook
import com.example.myapplication.R

/**
 * EbookScreen: Transforms the relaxation page into a professional E-book Library.
 * Users can browse a collection of books and read them in a dedicated full-screen reader.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookScreen() {
    // INSTRUCTION: Sample library data - in a real app, this would come from a JSON or Database
    val ebooks = remember {
        listOf(
            Ebook(
                "1", "Deep Work", "Cal Newport", 
                description = "Rules for focused success in a distracted world.",
                content = "Chapter 1: Deep Work is Valuable...\n\nIn the modern economy, the ability to focus without distraction on a cognitively demanding task is becoming increasingly rare. At the same time, it is becoming increasingly valuable. Those who cultivate this skill will thrive.\n\nChapter 2: Deep Work is Rare...\n\nMany workers spend their days in a frantic blur of e-mail and social media, not realizing that this 'shallow work' prevents them from producing at their peak capacity."
            ),
            Ebook(
                "2", "Atomic Habits", "James Clear",
                description = "An easy and proven way to build good habits and break bad ones.",
                content = "Introduction: My Story...\n\nTiny changes, remarkable results. A slight change in your daily habits can guide your life to a very different destination.\n\nChapter 1: The Surprising Power of Atomic Habits...\n\nSuccess is the product of daily habits—not once-in-a-lifetime transformations."
            ),
            Ebook(
                "3", "Focus", "Daniel Goleman",
                description = "The hidden driver of excellence.",
                content = "Attention is an underrated asset. In an era of unstoppable distractions, the ability to direct our focus is what separates high achievers from the rest.\n\nSelf-awareness is the key to managing focus. If you don't know where your mind is going, you can't bring it back."
            ),
            Ebook(
                "4", "The 5 AM Club", "Robin Sharma",
                description = "Own your morning, elevate your life.",
                content = "Victory is made in the early morning hours. While the world sleeps, the giants prepare.\n\nThe 20/20/20 formula: 20 minutes of exercise, 20 minutes of reflection, 20 minutes of growth."
            )
        )
    }

    // INSTRUCTION: State to track which book is currently being read
    var selectedBook by remember { mutableStateOf<Ebook?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredBooks = remember(searchQuery) {
        if (searchQuery.isEmpty()) ebooks 
        else ebooks.filter { it.title.contains(searchQuery, ignoreCase = true) || it.author.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Focus Library", fontWeight = FontWeight.Bold) }
                )
                // INSTRUCTION: Search bar for the library
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search books...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }
    ) { padding ->
        // INSTRUCTION: Grid layout for book covers
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(filteredBooks) { book ->
                BookItem(book) { selectedBook = book }
            }
        }
    }

    // INSTRUCTION: Full-screen Reader Overlay
    if (selectedBook != null) {
        EbookReader(selectedBook!!) { selectedBook = null }
    }
}

/**
 * BookItem: Represents a single book card in the library grid.
 */
@Composable
fun BookItem(book: Ebook, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Placeholder for Book Cover
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // INSTRUCTION: Using a colored box as a placeholder for a cover
                Surface(color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxSize()) {}
                Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(book.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

/**
 * EbookReader: A dedicated full-screen component for reading book content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookReader(book: Ebook, onBack: () -> Unit) {
    // INSTRUCTION: Dialog used to provide a focused, full-screen reading experience
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(book.title, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            // INSTRUCTION: Vertical scroll for long reading content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
                
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))
                
                // INSTRUCTION: The actual text content of the book
                Text(
                    text = book.content,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Justify
                )
                
                Spacer(modifier = Modifier.height(100.dp)) // Extra space at bottom for comfortable reading
            }
        }
    }
}
