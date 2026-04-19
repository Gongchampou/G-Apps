// LINE 1: This is the package name. It tells the app exactly where this file is located in the project.
package com.gongchampou.gapps.pages

// LINE 3: This empty line makes the code easier to read for humans.

// LINE 5: We import 'Animation' tools so that our progress bars can grow and shrink with smooth movement.
import androidx.compose.animation.core.*
// LINE 7: 'Canvas' is a tool that lets us draw custom shapes, like the spending circle, on the screen.
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
// LINE 9: 'Background' is used to set the color of the area behind our text and buttons.
import androidx.compose.foundation.background
// LINE 11: 'Layout' tools help us arrange our buttons and text in rows, columns, or boxes.
import androidx.compose.foundation.layout.*
// LINE 13: 'LazyColumn' is a special list that only loads items you can see, saving your phone's memory.
import androidx.compose.foundation.lazy.LazyColumn
// LINE 15: 'items' is a helper that loops through your list of money entries to show them one by one.
import androidx.compose.foundation.lazy.items
// LINE 17: 'RoundedCornerShape' makes the corners of our cards look smooth and modern instead of sharp.
import androidx.compose.foundation.shape.RoundedCornerShape
// LINE 19: 'KeyboardOptions' lets us tell the phone to show a number pad when the user enters money.
import androidx.compose.foundation.text.KeyboardOptions
// LINE 21: 'Icons' are the small pictures (like the trash can or plus sign) used throughout the app.
import androidx.compose.material.icons.Icons
// LINE 23: 'ArrowBack' is the standard icon for the "Back" button to return to the previous screen.
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// LINE 25: 'Add' is the plus sign icon used for creating new entries or setting limits.
import androidx.compose.material.icons.filled.Add
// LINE 27: 'Delete' is the trash can icon used to remove spending entries from your list.
import androidx.compose.material.icons.filled.Delete
// LINE 29: 'Material3' is Google's latest design system, providing beautiful buttons, cards, and bars.
import androidx.compose.material3.*
// LINE 31: 'runtime' tools like 'remember' and 'mutableStateOf' help the app remember what's happening.
import androidx.compose.runtime.*
// LINE 33: 'Alignment' helps us center things or push them to the sides of the screen.
import androidx.compose.ui.Alignment
// LINE 35: 'Modifier' is a "decorator" that lets us change the size, padding, or color of any UI piece.
import androidx.compose.ui.Modifier
// LINE 37: 'clip' is used to cut shapes (like circles) out of square boxes.
import androidx.compose.ui.draw.clip
// LINE 39: 'Offset' and 'Size' are math tools used for drawing exactly where shapes should go on screen.
import androidx.compose.ui.geometry.Offset
// LINE 41: 'Size' helps the Canvas know how wide and tall to draw our spending circle.
import androidx.compose.ui.geometry.Size
// LINE 43: 'Color' allows us to choose specific shades like Green for "Safe" and Red for "Over Limit".
import androidx.compose.ui.graphics.Color
// LINE 45: 'StrokeCap.Round' makes the ends of our circular progress bar look smooth and rounded.
import androidx.compose.ui.graphics.StrokeCap
// LINE 47: 'Stroke' tells the Canvas to draw an outline (like a ring) instead of a solid filled circle.
import androidx.compose.ui.graphics.drawscope.Stroke
// LINE 49: 'FontWeight' lets us make text Bold so it stands out and is easier to read.
import androidx.compose.ui.text.font.FontWeight
// LINE 51: 'KeyboardType.Number' ensures the user can only type numbers into the amount field.
import androidx.compose.ui.text.input.KeyboardType
// LINE 53: 'dp' (Density-independent Pixels) is a measurement unit that looks the same on all phones.
import androidx.compose.ui.unit.dp
// LINE 55: 'sp' (Scale-independent Pixels) is the standard unit for measuring text size on Android.
import androidx.compose.ui.unit.sp
// LINE 57: 'MoneyEntry' is the "Template" or "Model" for a single spending record (Amount, Date, etc).
import com.gongchampou.gapps.MoneyEntry
// LINE 59: 'TaskViewModel' is the "Brain" of the app that handles all the math and database work.
import com.gongchampou.gapps.TaskViewModel
// LINE 61: 'SimpleDateFormat' helps us turn computer timestamps into readable dates like "Oct 25".
import java.text.SimpleDateFormat
// LINE 63: 'java.util.*' provides standard tools for dates and lists used throughout the code.
import java.util.*

// LINE 66: '@Composable' tells Android that this function describes a piece of the user interface.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
// LINE 69: 'MoneyTrackingScreen' is the main "Blueprint" for the entire money management page.
fun MoneyTrackingScreen(viewModel: TaskViewModel, onBack: () -> Unit) {
    // LINE 71: We observe 'moneyEntries' from the brain so the list updates when you add or delete items.
    val moneyEntries by viewModel.moneyEntries.collectAsState()
    // LINE 73: 'moneyLimit' is the maximum amount of money the user wants to spend this month.
    val moneyLimit by viewModel.moneyLimit.collectAsState()
    // LINE 75: 'moneySpent' is the total sum of all the expenses you have added to the list.
    val moneySpent by viewModel.moneySpent.collectAsState()
    // LINE 77: 'showCircularView' tells the app whether to show the big circle or a simple summary card.
    val showCircularView by viewModel.showCircularProgress.collectAsState()
    // LINE 79: 'currency' is the symbol (like $ or ₹) that appears next to your money amounts.
    val currency by viewModel.currency.collectAsState()

    // LINE 82: 'showAddDialog' is a "Light Switch" that turns on the popup window for adding expenses.
    var showAddDialog by remember { mutableStateOf(false) }
    // LINE 84: 'showLimitDialog' is a "Light Switch" for the popup where you set your monthly budget.
    var showLimitDialog by remember { mutableStateOf(false) }
    
    // LINE 87: 'entryToDelete' remembers which specific item you clicked to delete, so we can ask for confirmation.
    var entryToDelete by remember { mutableStateOf<MoneyEntry?>(null) }
    // LINE 89: 'deleteConfirmText' stores what the user types to confirm they really want to delete an item.
    var deleteConfirmText by remember { mutableStateOf("") }

    // BLINKING ANIMATION: For when the budget is exceeded.
    val infiniteTransition = rememberInfiniteTransition(label = "budgetBlink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    // LINE 104: 'Scaffold' creates the standard layout structure with a top bar and a floating button.
    Scaffold(
        // LINE 94: 'topBar' is the area at the very top that shows the page title and back button.
        topBar = {
            // LINE 96: 'TopAppBar' is a Material Design 3 component for the header of the screen.
            TopAppBar(
                // LINE 98: We set the title to "Money Tracking" and make it Bold so it is clearly visible.
                title = { Text("Money Tracking", fontWeight = FontWeight.Bold) },
                // LINE 100: 'windowInsets' are set to zero to remove extra white space at the top of the screen.
                windowInsets = WindowInsets(0, 0, 0, 0),
                // LINE 102: 'navigationIcon' is the back arrow that lets the user return to the home screen.
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // LINE 108: 'actions' are buttons on the right side. Here, it is the button to set your budget.
                actions = {
                    IconButton(onClick = { showLimitDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Set Limit")
                    }
                }
            )
        },
        // LINE 116: 'floatingActionButton' is the big circular (+) button that floats in the bottom corner.
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { innerPadding ->
        // LINE 123: 'Column' stacks the UI elements one on top of the other like building blocks.
        Column(
            modifier = Modifier
                .padding(innerPadding) // This ensures our content doesn't get hidden behind the top bar.
                .padding(horizontal = 16.dp) // Adds a little breathing room on the left and right sides.
                .fillMaxSize(), // Makes the column take up the whole screen.
            horizontalAlignment = Alignment.CenterHorizontally // Centers the circle and list in the middle.
        ) {
            // LINE 131: Check if the user prefers the big circle visualizer or a text summary.
            if (showCircularView) {
                // LINE 133: Show the custom circular progress bar we designed below.
                CircularMoneyProgress(
                    spent = moneySpent,
                    limit = moneyLimit,
                    currency = currency,
                    modifier = Modifier.padding(bottom = 16.dp) // Reduced space to fit more list items.
                )
            } else {
                // LINE 141: If not using the circle, show a 'Card' with the total spent and a progress line.
                val progress = if (moneyLimit > 0) (moneySpent / moneyLimit) else 0f
                val remaining = moneyLimit - moneySpent
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        // LINE 145: If you spend more than your limit, the card turns Red/Orange as a warning.
                        containerColor = when {
                            progress >= 1f -> MaterialTheme.colorScheme.errorContainer
                            progress >= 0.8f -> Color(0xFFFFEBEE) // Very Light Red
                            progress >= 0.5f -> Color(0xFFFFF3E0) // Light Orange
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    // LINE 151: Inside the card, we show the numbers clearly.
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Spent: $currency${String.format("%.2f", moneySpent)}", style = MaterialTheme.typography.titleMedium)
                        if (moneyLimit > 0f) {
                            val remainingText = if (remaining < 0) 
                                "-$currency${String.format("%.2f", -remaining)}" 
                            else 
                                "Remaining: $currency${String.format("%.2f", remaining)}"
                            
                            Text(
                                text = remainingText, 
                                style = MaterialTheme.typography.bodySmall,
                                color = if (remaining < 0) Color.Red else Color.Unspecified,
                                fontWeight = if (remaining < 0) FontWeight.Bold else FontWeight.Normal
                            )
                            // LINE 155: A straight horizontal line that fills up as you spend more money.
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                color = when {
                                    progress >= 1f -> Color.Red.copy(alpha = blinkAlpha)
                                    progress >= 0.8f -> Color.Red
                                    progress >= 0.5f -> Color(0xFFFFA500) // Orange
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }
            }

            // LINE 164: Show a text alert if budget is reaching high levels (80%+).
            val currentProgress = if (moneyLimit > 0) (moneySpent / moneyLimit) else 0f
            if (currentProgress >= 0.8f) {
                Text(
                    text = if (currentProgress >= 1f) "🚨 BUDGET EXCEEDED!" else "⚠️: 80% REACHED",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // LINE 176: A header title for the list of spending history.
            Text("Entries", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp))

            // LINE 169: 'LazyColumn' is the most efficient way to show a long list on Android.
            LazyColumn(
                modifier = Modifier.weight(1f), // This tells the list to take up all available space.
                verticalArrangement = Arrangement.spacedBy(4.dp) // Puts a tiny gap between each item.
            ) {
                // LINE 174: We loop through every single spending entry and create a "Card" for it.
                items(moneyEntries) { entry ->
                    // LINE 176: Instead of deleting immediately, we now set 'entryToDelete' to trigger the GitHub-style confirm.
                    MoneyEntryItem(entry = entry, currency = currency, onDelete = { 
                        entryToDelete = entry 
                        deleteConfirmText = "" // Reset the confirmation text every time.
                    })
                }
            }
        }

        // LINE 185: Logic for the "Add Expense" popup window.
        if (showAddDialog) {
            AddMoneyEntryDialog(onDismiss = { showAddDialog = false }, onAdd = { amount, desc -> viewModel.addMoneyEntry(amount, desc); showAddDialog = false })
        }

        // LINE 190: Logic for the "Set Budget Limit" popup window.
        if (showLimitDialog) {
            SetLimitDialog(
                currentLimit = moneyLimit, 
                onDismiss = { showLimitDialog = false }, 
                onSave = { limit -> viewModel.setMoneyLimit(limit); showLimitDialog = false },
                onResetAll = { viewModel.resetMoneyData() }
            )
        }
        
        // LINE 195: GITHUB-STYLE DELETE CONFIRMATION (The "Asking Twice" part).
        if (entryToDelete != null) {
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                // LINE 199: The Title warning the user about permanent deletion.
                title = { Text("Are you absolutely sure?", color = Color.Red, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        // LINE 203: Explaining exactly what will happen.
                        Text("This action cannot be undone. This will permanently delete the entry for '${entryToDelete?.description}'.")
                        Spacer(modifier = Modifier.height(12.dp))
                        // LINE 206: The "GitHub Style" requirement: typing the description to confirm.
                        Text("Please type the description below to confirm:", style = MaterialTheme.typography.labelSmall)
                        OutlinedTextField(
                            value = deleteConfirmText,
                            onValueChange = { deleteConfirmText = it },
                            placeholder = { Text(entryToDelete?.description ?: "") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            singleLine = true
                        )
                    }
                },
                // LINE 218: The button that actually deletes the data.
                confirmButton = {
                    Button(
                        // LINE 221: Only enable the button if the user typed the description PERFECTLY.
                        enabled = deleteConfirmText == entryToDelete?.description,
                        onClick = {
                            entryToDelete?.let { viewModel.removeMoneyEntry(it) }
                            entryToDelete = null // Close the dialog after success.
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("I understand, delete this entry")
                    }
                },
                // LINE 231: The button to back out and keep the data safe.
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// LINE 241: This function draws the Circular Progress bar seen at the top of the screen.
@Composable
fun CircularMoneyProgress(spent: Float, limit: Float, currency: String, modifier: Modifier = Modifier) {
    // LINE 244: Math: Calculate what percentage of the budget has been used (from 0.0 to 1.0).
    val progress = if (limit > 0) (spent / limit).coerceIn(0f, 1f) else 0f
    // LINE 246: Math: Calculate how much money is left (can be negative if over budget).
    val remaining = limit - spent
    
    // LINE 249: 'animateFloatAsState' makes the circle "grow" smoothly when you add an expense.
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "moneyProgress"
    )

    // BLINKING ANIMATION for exceeded limit
    val infiniteTransition = rememberInfiniteTransition(label = "circularBlink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    // Determine color based on spending percentage: Green < 50%, Orange 50-80%, Red > 80%
    val progressColor = when {
        progress >= 0.8f -> Color.Red
        progress >= 0.5f -> Color(0xFFFFA500) // Orange
        else -> Color(0xFF00C853) // Green
    }

    // LINE 256: 'Box' puts the text (Amount Left) right in the middle of the circle.
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(180.dp)) {
        // LINE 258: 'Canvas' is where we draw the actual shapes.
        Canvas(modifier = Modifier.fillMaxSize()) {
            // LINE 260: Calculate sizes so the circle fits perfectly without being cut off.
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)

            // LINE 266: Draw the "Shadow" circle (Gray) so you can see the full track.
            drawArc(color = Color.LightGray.copy(alpha = 0.3f), startAngle = 135f, sweepAngle = 270f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            // LINE 268: Draw the "Filled" circle (Green/Orange/Red) based on how much you spent.
            // It blinks if we are at 100% or more.
            val finalAlpha = if (spent >= limit && limit > 0) blinkAlpha else 1f
            drawArc(color = progressColor.copy(alpha = finalAlpha), startAngle = 135f, sweepAngle = 270f * animatedProgress, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        }
        // LINE 271: The text in the center of the circle.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val statusText = when {
                spent >= limit && limit > 0 -> "Over Limit"
                progress >= 0.8f -> "Alert!"
                progress >= 0.5f -> "Warning"
                else -> "Safe"
            }
            val statusColor = if (progress >= 0.8f) Color.Red else Color.Gray

            Text(text = statusText, style = MaterialTheme.typography.labelSmall, color = statusColor)
            
            val remainingText = if (remaining < 0) 
                "-$currency${String.format("%.2f", -remaining)}" 
            else 
                "$currency${String.format("%.2f", remaining)}"
                
            Text(text = remainingText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (remaining < 0) Color.Red else Color.Unspecified)
        }
    }
}

// LINE 280: This function describes what one single item in the spending list looks like.
@Composable
fun MoneyEntryItem(entry: MoneyEntry, currency: String, onDelete: () -> Unit) {
    // LINE 283: We remember the date format so we don't have to recreate it every time the screen flickers.
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    // LINE 286: Each entry is inside a 'Card' to give it a nice border and shadow.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        // LINE 292: 'Row' puts the description on the left and the price/trash on the right.
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LINE 297: The left side: Description and Date.
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(dateFormat.format(Date(entry.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            // LINE 302: The middle: The price of the item.
            Text("$currency${String.format("%.2f", entry.amount)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            // LINE 305: The right side: The trash can button to delete.
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

// LINE 314: The popup dialog that appears when you want to add a new expense.
@Composable
fun AddMoneyEntryDialog(onDismiss: () -> Unit, onAdd: (Float, String) -> Unit) {
    // LINE 317: These 'states' remember what you typed into the boxes.
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // LINE 321: 'AlertDialog' is the standard Android component for popups.
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // LINE 329: Text box for what the money was spent on.
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("What for?") }, modifier = Modifier.fillMaxWidth())
                // LINE 327: Text box for the amount.
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            }
        },
        // LINE 333: The "Add" button only works if you typed a valid number.
        confirmButton = { Button(onClick = { val amount = amountStr.toFloatOrNull() ?: 0f; if (amount > 0 && description.isNotBlank()) onAdd(amount, description) }) { Text("Add") } },
        // LINE 335: The "Cancel" button closes the window without saving anything.
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// LINE 340: The popup dialog that appears when you want to change your budget limit.
@Composable
fun SetLimitDialog(currentLimit: Float, onDismiss: () -> Unit, onSave: (Float) -> Unit, onResetAll: () -> Unit) {
    // LINE 343: Remembers the budget you typed.
    var limitStr by remember { mutableStateOf(if (currentLimit > 0) currentLimit.toString() else "") }
    // LINE 345: 'showResetConfirm' is a light switch to show the "Are you REALLY sure?" reset dialog.
    var showResetConfirm by remember { mutableStateOf(false) }
    // LINE 347: 'resetConfirmText' stores the word "RESET" that the user must type.
    var resetConfirmText by remember { mutableStateOf("") }

    if (showResetConfirm) {
        // LINE 351: GITHUB-STYLE RESET CONFIRMATION (The ultimate safety net).
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Data?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("This will delete EVERY entry in your history and back to zero.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Type 'RESET' to confirm:", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = resetConfirmText,
                        onValueChange = { resetConfirmText = it },
                        placeholder = { Text("RESET") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    // LINE 369: Only works if they type 'RESET' exactly.
                    enabled = resetConfirmText == "RESET",
                    onClick = {
                        onResetAll()
                        showResetConfirm = false
                        onDismiss() // Close the main dialog too
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Budget Settings", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // STEP 1: Monthly Budget Input
                // This is where you set the maximum amount you want to spend each month.
                OutlinedTextField(
                    value = limitStr, 
                    onValueChange = { limitStr = it }, 
                    label = { Text("Monthly Limit Amount") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                    modifier = Modifier.fillMaxWidth()
                )
                
                // STEP 2: The "Danger Zone" Reset Button
                // Use this ONLY if you want to wipe your entire history and start from zero!
                // We made it look like a nice bordered button so it's easy to find but clearly for "Resetting".
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red.copy(alpha = 0.7f) // Faded red to show it is a "Danger" action.
                    ),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)), // A soft border to keep it looking clean.
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                    shape = RoundedCornerShape(8.dp) // Smooth corners to match the rest of the app.
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reset Data", style = MaterialTheme.typography.labelMedium)
                }
            }
        },
        // STEP 3: Save Your New Limit
        confirmButton = { 
            Button(onClick = { 
                val limit = limitStr.toFloatOrNull() ?: 0f
                onSave(limit) 
            }) { Text("Save Limit") } 
        },
        // STEP 4: Close without changing anything
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Close") } 
        }
    )
}
