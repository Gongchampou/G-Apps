package com.example.myapplication.pages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.RelaxTrick

@Composable
fun RelaxScreen() {
    var isBreathing by remember { mutableStateOf(false) }
    val tricks = remember {
        listOf(
            RelaxTrick(
                "Military Method",
                Icons.Default.NightsStay,
                "Fall asleep in 2 minutes. Used by pilots to sleep in stressful environments.",
                listOf(
                    "Relax your entire face, including muscles inside your mouth.",
                    "Drop your shoulders to release tension.",
                    "Exhale, relaxing your chest.",
                    "Relax your legs, thighs, and calves.",
                    "Clear your mind for 10 seconds by imagining a relaxing scene."
                )
            ),
            RelaxTrick(
                "Progressive Muscle Relaxation",
                Icons.Default.AccessibilityNew,
                "Tense and release muscle groups to physically let go of stress.",
                listOf(
                    "Start at your toes: curl them tightly for 5 seconds.",
                    "Suddenly release and feel the tension flow out for 10 seconds.",
                    "Move up to your calves, then thighs, then glutes.",
                    "Continue to your stomach, chest, arms, and finally your face.",
                    "Notice the contrast between tension and relaxation."
                )
            ),
            RelaxTrick(
                "5-4-3-2-1 Grounding",
                Icons.Default.Visibility,
                "Get out of your head and into your surroundings to stop anxiety.",
                listOf(
                    "Acknowledge 5 things you see around you.",
                    "Acknowledge 4 things you can touch.",
                    "Acknowledge 3 things you hear.",
                    "Acknowledge 2 things you can smell.",
                    "Acknowledge 1 thing you can taste."
                )
            ),
            RelaxTrick(
                "Paradoxical Intention",
                Icons.Default.Psychology,
                "Try to stay awake! By reducing the pressure to sleep, you fall asleep faster.",
                listOf(
                    "Lie in bed and keep your eyes open.",
                    "Tell yourself 'I am not going to sleep'.",
                    "Stop fighting to fall asleep.",
                    "This removes performance anxiety about sleep.",
                    "Your body will naturally take over and drift off."
                )
            ),
            RelaxTrick(
                "Brain Dump",
                Icons.Default.EditNote,
                "Clear your mind of worries and to-dos before bed.",
                listOf(
                    "Get a piece of paper or use the Todo tab.",
                    "Write down everything on your mind.",
                    "Don't worry about order or grammar.",
                    "Once it's on paper, your brain stops 'looping' on it.",
                    "Tell yourself you will handle it tomorrow."
                )
            )
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Relaxation & Sleep", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Featured: 4-7-8 Breathing", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    BreathingExercise(isBreathing)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { isBreathing = !isBreathing },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isBreathing) Color.Gray else Color(0xFF34C759))
                    ) {
                        Text(if (isBreathing) "Stop Session" else "Start Breathing")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sleep & Relaxation Tricks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
        }

        items(tricks) { trick ->
            TrickCard(trick)
        }
        
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TrickCard(trick: RelaxTrick) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(trick.icon, null, modifier = Modifier.size(32.dp), tint = Color(0xFF007AFF))
                Spacer(modifier = Modifier.width(16.dp))
                Text(trick.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Text(
                text = trick.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                trick.steps.forEachIndexed { index, step ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("${index + 1}. ", fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                        Text(step, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(
                    "Tap to close",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.End)
                )
            } else {
                Text(
                    "Tap to see steps",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun BreathingExercise(active: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val sizeMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "size"
    )
    
    val text = if (!active) "Deep Calm" 
               else if (sizeMultiplier > 0.95f) "Hold..."
               else if (sizeMultiplier < 0.65f) "Breathe In..."
               else "Breathe Out..."

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF007AFF).copy(alpha = 0.2f),
                radius = (size * sizeMultiplier).minDimension / 2,
                style = Stroke(width = 8.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF007AFF).copy(alpha = 0.1f),
                radius = (size * sizeMultiplier).minDimension / 2.5f
            )
        }
        Text(text, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF007AFF), fontWeight = FontWeight.Medium)
    }
}
