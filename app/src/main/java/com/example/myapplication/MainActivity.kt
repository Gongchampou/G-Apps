package com.gongchampou.gapps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gongchampou.gapps.pages.*
import com.gongchampou.gapps.ui.theme.MyApplicationTheme

/**
 * MAIN ACTIVITY
 * This is the "brain" of your app. It's the first thing that runs when you open the app.
 * It sets up the theme (Dark/Light mode) and decides which screen to show first.
 */
class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // This makes the app go full screen (behind the status bar)
        setContent {
            // TIP: This line connects your app to the Dark Mode setting in your ViewModel.
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            
            MyApplicationTheme(darkTheme = isDarkMode) {
                // This part handles "Backgrounding" - it saves your timer if you close the app.
                DisposableEffect(Unit) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_STOP -> viewModel.onEnterBackground()
                            Lifecycle.Event.ON_START -> viewModel.onEnterForeground()
                            else -> {}
                        }
                    }
                    ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
                    onDispose {
                        ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
                    }
                }
                
                // Starts the main UI container
                MainScreen(viewModel)
            }
        }
    }
}

/**
 * THE SCREEN LIST
 * This is where you define every page in your app.
 * CHANGE: To change an icon, replace 'Icons.Default.List' with another one like 'Icons.Default.Home'.
 * CHANGE: To change the name shown in the menu, edit the text in "quotes".
 */
sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Todo : Screen("todo", Icons.Default.List, "Todo")
    object Timer : Screen("timer", Icons.Default.Timer, "Timer")
    object Music : Screen("music", Icons.Default.MusicNote, "Focus")
    object Relax : Screen("relax", Icons.AutoMirrored.Filled.MenuBook, "E-Book")
    object Settings : Screen("settings", Icons.Default.Settings, "Settings")
    object MoneyTracking : Screen("money_tracking", Icons.Default.AttachMoney, "Money")
    
    // These screens are hidden from the bottom bar but still part of the app logic
    object DownloadedMusic : Screen("downloaded_music", Icons.Default.Download, "Downloaded")
    object OnlineMusic : Screen("online_music", Icons.Default.CloudDownload, "Online")
}

/**
 * MAIN SCREEN CONTAINER
 * This holds the Bottom Bar and the actual page content.
 */
@Composable
fun MainScreen(viewModel: TaskViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            // TIP: This adds the navigation menu at the very bottom of the screen.
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        // NAV HOST: This is like a "Projector". It swaps screens based on what you click.
        NavHost(
            navController = navController,
            // CHANGE: Change 'Screen.Todo.route' to 'Screen.Timer.route' to start on the Timer instead.
            startDestination = Screen.Todo.route, 
            modifier = Modifier.padding(innerPadding)
        ) {
            // Each 'composable' block links a "Route" to a "Screen File"
            composable(Screen.Todo.route) { 
                TodoScreen(viewModel, onNavigateToMoney = { navController.navigate(Screen.MoneyTracking.route) }) 
            }
            composable(Screen.Timer.route) { TimerScreen(viewModel) }
            composable(Screen.Music.route) { MusicScreen(navController) }
            composable(Screen.Relax.route) { EbookScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel, navController) }
            composable(Screen.MoneyTracking.route) { MoneyTrackingScreen(viewModel, onBack = { navController.popBackStack() }) }
            
            // Sub-pages for the Music screen
            composable(Screen.DownloadedMusic.route) { 
                DownloadedMusicScreen(onBack = { navController.popBackStack() }) 
            }
            composable(Screen.OnlineMusic.route) {
                OnlineMusicScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/**
 * BOTTOM NAVIGATION BAR
 * This is the UI for the menu buttons at the bottom.
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // CHANGE: To reorder the buttons, simply move the items in this list!
    // Example: listOf(Screen.Music, Screen.Todo, Screen.Timer...)
    val items = listOf(Screen.Todo, Screen.Timer, Screen.Music, Screen.Relax, Screen.Settings)
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        tonalElevation = 8.dp, // Adds a slight shadow to make it pop
        // CHANGE: Adjust '20.dp' to make the top corners more or less rounded.
        modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { 
                    Text(
                        screen.label, 
                        style = MaterialTheme.typography.labelLarge, // Slightly bigger text
                        fontWeight = FontWeight.Bold // Make it stand out
                    ) 
                },
                selected = currentRoute == screen.route,
                onClick = {
                    // Logic to jump to the clicked screen
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                // CHANGE: Use theme colors for better visibility in both Light and Dark mode
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}
