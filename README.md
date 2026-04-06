# My Application - Ebook & Prayer App

A modern Android application built with Jetpack Compose for reading ebooks, prayers, and stories. This app features a dynamic library loaded from JSON, an integrated reader with customizable font sizes, and support for high-quality SVG cover images.

## 🚀 Features

- **Dynamic Ebook Library**: A grid-based library that automatically populates from a `book.json` asset file.
- **Advanced Ebook Reader**: 
    - Full-text reading experience.
    - Customizable font sizes (12sp to 30sp).
    - Smooth scrolling and "Back" navigation.
- **Rich Media Support**:
    - **SVG Covers**: Support for both SVG files in assets and inline SVG strings directly within the JSON data.
    - **Coil Integration**: Efficient image loading and caching using the Coil library.
- **Category Support**: Organize content into categories like Prayers (Morning/Night), Stories, Songs, etc.
- **Robust Data Parsing**: Uses Gson for reliable JSON-to-Model conversion with default value safety.
- **Material 3 UI**: Modern, clean interface following the latest Android design guidelines.

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) (with SVG and GIF support)
- **JSON Parsing**: [Gson](https://github.com/google/gson)
- **Architecture**: Modern Android Architecture with Composable state management.
- **Persistence**: Room Database and DataStore (Preferences).
- **Media**: Media3 (ExoPlayer) for potential audio/video content.

## 📂 Project Structure

- `app/src/main/java/com/example/myapplication/pages/`: Contains the UI screens (`EbookScreen.kt`, etc.).
- `app/src/main/java/com/example/myapplication/Models.kt`: Data models for the app.
- `app/src/main/assets/`: Contains `book.json` and other static content.

## 🔧 Recent Improvements

- **Stability Fix**: Resolved a `NullPointerException` when loading books with missing `coverImage` fields.
- **Model Robustness**: Updated the `Ebook` data class with default values to ensure Gson handles missing JSON fields gracefully.
- **SVG Rendering**: Implemented a custom `ImageLoader` with `SvgDecoder` to support vector covers.

## 📖 How to Add New Books

1. Open `app/src/main/assets/book.json`.
2. Add a new entry following this structure:
   ```json
   {
     "id": "unique_id",
     "title": "Book Title",
     "author": "Author Name",
     "category": "Story",
     "coverImage": "image_name.png", 
     "description": "Short description...",
     "content": "Full book content here (HTML supported)..."
   }
   ```
3. For SVG covers, you can paste the raw `<svg>...</svg>` code into the `"coverImage"` field.

## 🛠 Building the App

Standard Gradle build:
```bash
./gradlew assembleDebug
```

Developed with ❤️ in Android Studio.
