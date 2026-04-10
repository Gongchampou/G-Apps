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
    - **Visual Progress**: Dynamic circular progress bar that changes color (Green -orange-red) as the limit is approached or exceeded.
    - **progress status**: 0%-50% Green(safe) to 50%-79% Orange(warning) to 80%-100% Red(Over Limit)
    - **Double confirmation Deletion**: Secure deletion of entries requiring the user to type the description to confirm.
    - **Toggleable Views**: Switch between a detailed circular visualizer and a compact summary card.
    - **Persistent History**: Displays a list of recent spending entries. (data persist for one month, and until it reset from limit set)
- **Why it exists**: To help users stay within their budget by providing clear visual feedback and an easy-to-use logging system.

### 3. [TimerScreen.kt](./TimerScreen.kt)
**Purpose**: A tool for timed activities (e.g., meditation, timed prayer).
- **Features**:
    - **Count-down Logic**: Interactive timer with start/pause/reset.
    - **Emoji Logic**: To Stay focus and consistent with fund.
    - **Visual Progress**: Circular progress indicators.
    - **Tone Set**: i put the tone to change from the setting, not to use the device tone.

### 4. [MusicScreen.kt](./MusicScreen.kt)
**Purpose**: The main audio playback interface.
- **Features**:
    - **Media3 Integration**: Connects to a `MediaController` for robust background playback.
    - **Dynamic Playlist**: Loads tracks from a JSON source.
    - **Playback Controls**: Play/Pause, Skip, and Seek functionality.
    - **Visualizations**: Support for GIF/SVG album art and basic animations.
- **Why it exists**: To manage the playback of songs, chants, or guided prayers and focus.

### 5. [OnlineMusicScreen.kt](./OnlineMusicScreen.kt)
**Purpose**: Interface for streaming audio from remote servers.
- **Features**:
    - **Network Loading**: Fetches track lists from online APIs and store.
    - **Streaming**: Uses `Media3` to stream audio without local storage.
    - **Visualizations**: Supports GIF/SVG album art and basic animations.
    - **Local store**: Allows users to save tracks for offline playback (./app/assets/music/..)
    - **Json Data**: Put the song-list form the json file.
  -**why it exists**: To stay within the apps and focus.



### 6. [EbookScreen.kt](./EbookScreen.kt)
**Purpose**: The central hub for the digital library and reading experience.
- **Features**:
  - **Grid Library**: Displays books from `book.json` using `LazyVerticalGrid`.
  - **Category Filtering**: Allows users to switch between different book categories.
  - **Integrated Reader**: A full-screen overlay for reading book content with adjustable font sizes.
  - **SVG Support**: Handles inline SVG strings for book covers.
  - **Theme Support**: It support dark or read mode to (protect eye) from blue ray of the book.
- **Why it exists**: To provide a clean, accessible way for users to browse and read text-heavy content like stories and prayers.


### 7. [SettingsScreen.kt](./SettingsScreen.kt)
**Purpose**: Application configuration.
- **Features**:
    - **Theme Toggles**: Light/Dark mode.
    - **Notification** : Alarm for daily reminders.
    - **Vibration**: App vibration (to off and on).
    - **sound**: toggles sound on and off.
    - **screen**: toggles screen on and off.(to keep awake the screen evne the device have limit.).
    - **Money Tracking** toggle for circle and linear view progress.
    - **Currency**: Too set the Currency of their own i.e. $,₹,€,¥...
    - **Font Preferences**: Global font scaling for Ebook reader.
    - **About Section**: App version and developer info.
    - **Git repo**: Link to the GitHub repository.

    
### 8. [DownloadedMusicScreen.kt](./DownloadedMusicScreen.kt)
**Purpose**: Offline access to audio content.
- **Features**:
  - **Local Storage Management**: Lists files stored in the app's internal/external storage.
  - **Offline Playback**: Ensures music works without an internet connection.
  - **Downloaded**: Downloaded Data stay in the app Database and if delete then it delete from the storage also to safe other miss-use of storage. 





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
