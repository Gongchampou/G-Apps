# App Pages Documentation

This directory contains the core UI screens of the application. Each screen is built using **Jetpack Compose** and follows a modular design.

## 📱 Available Screens

### 1. [TodoScreen.kt](./TodoScreen.kt)
**Purpose**: A productivity tool for managing Task or personal tasks.
- **Features**:
    - **Task List**: CRUD operations for tasks.
    - **Persistence**: Likely backed by Room database for saving state.
    - **Fun style**: A bubble will drop down from the top fo the screen when you complete the task of the incomplete task.

### 2. [MoneyTrackingScreen.kt](./MoneyTrackingScreen.kt)
**Purpose**: A tool for managing and tracking personal expenses and budgets.
- **Features**:
    - **Budget Management**: Set a monthly spending limit with a dedicated dialog.
    - **Expense Tracking**: Add expenses with descriptions and amounts.
    - **Visual Progress**: Dynamic circular progress bar that changes color (Green to Red) as the limit is approached or exceeded.
    - **GitHub-Style Deletion**: Secure deletion of entries requiring the user to type the description to confirm.
    - **Toggleable Views**: Switch between a detailed circular visualizer and a compact summary card.
    - **Persistent History**: Displays a list of recent spending entries.
- **Why it exists**: To help users stay within their budget by providing clear visual feedback and an easy-to-use logging system.

### 3. [TimerScreen.kt](./TimerScreen.kt)
**Purpose**: A tool for timed activities (e.g., meditation, timed prayer).
- **Features**:
    - **Count-down Logic**: Interactive timer with start/pause/reset.
    - **Visual Progress**: Circular progress indicators.

### 4. [MusicScreen.kt](./MusicScreen.kt)
**Purpose**: The main audio playback interface.
- **Features**:
    - **Media3 Integration**: Connects to a `MediaController` for robust background playback.
    - **Dynamic Playlist**: Loads tracks from a JSON source.
    - **Playback Controls**: Play/Pause, Skip, and Seek functionality.
    - **Visualizations**: Support for GIF/SVG album art and basic animations.
- **Why it exists**: To manage the playback of songs, chants, or guided prayers.

### 5. [OnlineMusicScreen.kt](./OnlineMusicScreen.kt)
**Purpose**: Interface for streaming audio from remote servers.
- **Features**:
    - **Network Loading**: Fetches track lists from online APIs.
    - **Streaming**: Uses `Media3` to stream audio without local storage.



### 6. [EbookScreen.kt](./EbookScreen.kt)
**Purpose**: The central hub for the digital library and reading experience.
- **Features**:
  - **Grid Library**: Displays books from `book.json` using `LazyVerticalGrid`.
  - **Category Filtering**: Allows users to switch between different book categories.
  - **Integrated Reader**: A full-screen overlay for reading book content with adjustable font sizes.
  - **SVG Support**: Handles inline SVG strings for book covers.
- **Why it exists**: To provide a clean, accessible way for users to browse and read text-heavy content like stories and prayers.


### 7. [SettingsScreen.kt](./SettingsScreen.kt)
**Purpose**: Application configuration.
- **Features**:
    - **Theme Toggles**: Light/Dark mode.
    - **Font Preferences**: Global font scaling.
    - **About Section**: App version and developer info.

    
### 8. [DownloadedMusicScreen.kt](./DownloadedMusicScreen.kt)
**Purpose**: Offline access to audio content.
- **Features**:
  - **Local Storage Management**: Lists files stored in the app's internal/external storage.
  - **Offline Playback**: Ensures music works without an internet connection.





---

## 🛠 Instructions for Development

1. **Adding a New Screen**:
   - Create a new `.kt` file in this directory.
   - Use the `@Composable` annotation.
   - Register the new screen in your main navigation host (usually in `MainActivity.kt` or a `NavHost`).

2. **State Management**:
   - Prefer `rememberSaveable` for simple UI state.
   - Use `ViewModel` for complex business logic (e.g., fetching data from JSON/API).

3. **Styling**:
   - Always use `MaterialTheme.colorScheme` and `MaterialTheme.typography` to ensure consistency with the app's theme.
