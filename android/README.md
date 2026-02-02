# Snap Android App

Native Android implementation of the Snap video downloader, built with Kotlin, Jetpack Compose, and MVVM architecture.

## Project Structure

```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/snap/
│   │   │   │   ├── SnapApplication.kt           # App initialization
│   │   │   │   ├── MainActivity.kt              # Main activity
│   │   │   │   ├── data/
│   │   │   │   │   ├── models/
│   │   │   │   │   │   └── VideoModels.kt       # Data classes
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── SnapApiService.kt    # Retrofit service
│   │   │   │   │   │   └── ApiServiceFactory.kt # API configuration
│   │   │   │   │   └── repository/
│   │   │   │   │       └── VideoRepository.kt   # Data access layer
│   │   │   │   └── ui/
│   │   │   │       ├── theme/
│   │   │   │       │   └── Theme.kt             # Compose theme
│   │   │   │       ├── viewmodel/
│   │   │   │       │   ├── VideoViewModel.kt
│   │   │   │       │   ├── DownloadViewModel.kt
│   │   │   │       │   └── HistoryViewModel.kt
│   │   │   │       └── screen/
│   │   │   │           ├── InputScreen.kt       # URL input screen
│   │   │   │           ├── VideoInfoScreen.kt   # Video details screen
│   │   │   │           ├── DownloadScreen.kt    # Download progress screen
│   │   │   │           └── HistoryScreen.kt     # Download history screen
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── kotlin/com/snap/
│   │           ├── data/
│   │           │   ├── api/SnapApiServiceTest.kt
│   │           │   └── repository/VideoRepositoryTest.kt
│   │           └── ui/viewmodel/VideoViewModelTest.kt
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md (this file)
```

## Architecture

### MVVM (Model-View-ViewModel)
- **Model**: Data classes and repository layer
- **View**: Jetpack Compose UI screens
- **ViewModel**: State management and business logic

### Layers
1. **Presentation Layer** (UI)
   - Compose screens
   - ViewModels (state management)
   - Theme and components

2. **Data Layer**
   - Repository pattern
   - Retrofit API service
   - Data models

3. **Domain Layer** (Implicit)
   - Use cases for download, playback, history
   - Business logic abstraction

## Tech Stack

### UI & Composition
- **Jetpack Compose** 1.6.0 - Modern declarative UI
- **Material Design 3** - Material 3 components
- **Compose Navigation** - Screen navigation

### Networking
- **Retrofit 2.10.0** - REST client
- **OkHttp 4.11.0** - HTTP client
- **Kotlinx Serialization** - JSON serialization

### Data & Storage
- **Room 2.6.1** - Local database
- **DataStore** - Preferences storage

### Concurrency
- **Kotlin Coroutines 1.7.3** - Async operations
- **Flow** - Reactive streams

### Lifecycle & State
- **Jetpack ViewModel** 2.6.2 - State management
- **Jetpack Lifecycle** 2.6.2 - Lifecycle awareness

### Media
- **Media3 ExoPlayer** 1.1.1 - Video/audio playback
- **Media3 UI** 1.1.1 - Player controls

### Testing
- **JUnit 4** - Unit testing
- **MockK** - Mocking library
- **Espresso** - UI testing

## Setup Instructions

### Prerequisites
- Android Studio 2023.1+
- JDK 17+
- Android SDK 29+ (API level)
- Kotlin 1.9.22+

### Initial Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/MikhaelGois/snap.git
   cd snap/android
   ```

2. **Open in Android Studio**
   - File → Open
   - Select the `android` directory
   - Let Gradle sync complete

3. **Configure Backend**
   - Edit `app/src/main/kotlin/com/snap/data/api/ApiServiceFactory.kt`
   - Update `DEFAULT_BASE_URL` if backend is not on localhost:5000
   ```kotlin
   private const val DEFAULT_BASE_URL = "http://your-server:5000"
   ```

4. **Build and Run**
   ```bash
   # Build debug APK
   ./gradlew assembleDebug
   
   # Run on emulator or device
   ./gradlew installDebug
   ```

## API Integration

### Endpoints Used

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/` | Health check |
| POST | `/api/video-info` | Get video metadata |
| POST | `/api/download` | Start download |
| GET | `/api/download-status/{id}` | Get progress |
| GET | `/api/history` | Fetch history |
| POST | `/api/history/clear` | Clear history |

### Request/Response Models

See `data/models/VideoModels.kt` for complete model definitions:
- `VideoInfo` - Video metadata with chapters
- `DownloadProgress` - Download status and progress
- `DownloadHistory` - Historical downloads
- `Chapter` - Chapter information

## Features (Planned)

### Phase 1: Core Functionality ✅
- [x] Video info extraction
- [x] Single file download
- [x] Separated chapter download
- [x] Download progress monitoring
- [x] Download history

### Phase 2: UI & UX
- [ ] Input screen with URL validation
- [ ] Video info display with chapter preview
- [ ] Download options (format, quality, mode)
- [ ] Real-time progress indicator
- [ ] Completion notification
- [ ] Media player integration
- [ ] History listing

### Phase 3: Advanced Features
- [ ] Offline-first with sync
- [ ] Batch URL downloads
- [ ] Download pause/resume
- [ ] File management
- [ ] Quality auto-selection

### Phase 4: Polish
- [ ] Settings screen
- [ ] Dark/light theme toggle
- [ ] App shortcuts
- [ ] Widget support
- [ ] Share functionality

## State Management

### ViewModel States

**VideoViewModel**
```kotlin
data class VideoInfoUiState(
    val isLoading: Boolean = false,
    val videoInfo: VideoInfo? = null,
    val error: String? = null,
    val selectedChapters: Set<Int> = emptySet(),
    val selectedQuality: DownloadQuality = DownloadQuality.BEST,
    val selectedFormat: DownloadFormat = DownloadFormat.MP4,
    val downloadMode: DownloadMode = DownloadMode.SINGLE_FILE
)
```

**DownloadViewModel**
```kotlin
data class DownloadUiState(
    val isDownloading: Boolean = false,
    val downloadId: String? = null,
    val progress: DownloadProgress? = null,
    val error: String? = null,
    val isCompleted: Boolean = false
)
```

**HistoryViewModel**
```kotlin
data class HistoryUiState(
    val isLoading: Boolean = false,
    val history: List<DownloadHistory> = emptyList(),
    val error: String? = null,
    val isClearing: Boolean = false,
    val clearMessage: String? = null
)
```

## Networking

### Configuration

Retrofit is configured with:
- **Base URL**: `http://localhost:5000` (configurable)
- **Timeout**: 30 seconds
- **Converter**: Kotlinx Serialization
- **Interceptors**: Logging (debug mode)
- **Retry**: Connection failure retry enabled

### Error Handling

Results are wrapped in sealed class:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Test Examples
- `VideoRepositoryTest.kt` - Repository layer
- `SnapApiServiceTest.kt` - API service
- `VideoViewModelTest.kt` - ViewModel logic

## Build Variants

### Debug Build
- Debuggable
- Logging enabled
- No obfuscation

### Release Build
- ProGuard enabled
- Minification enabled
- Resource shrinking enabled
- Unsigned (must sign before distribution)

## Release Build

```bash
# Generate signed APK
./gradlew bundleRelease

# Or APK
./gradlew assembleRelease
```

## Gradle Configuration

### Version Properties

Edit `gradle.properties` to adjust versions:
- SDK versions (compile, min, target)
- Kotlin version
- Compose version
- Dependency versions

### Dependencies

Key dependencies:
- Core Android: `androidx.core:core-ktx`
- Compose: `androidx.compose.*`
- Network: `com.squareup.retrofit2`, `com.squareup.okhttp3`
- Database: `androidx.room:room-ktx`
- Coroutines: `org.jetbrains.kotlinx:kotlinx-coroutines-*`

## Contributing

1. Create a feature branch (`git checkout -b feature/your-feature`)
2. Commit changes (`git commit -am 'Add feature'`)
3. Push to branch (`git push origin feature/your-feature`)
4. Create Pull Request

## License

MIT License - See LICENSE file in root directory

## Support

For issues or questions:
- GitHub Issues: https://github.com/MikhaelGois/snap/issues
- Backend Issues: See main Snap project

## Roadmap

**Q1 2026**
- Complete core UI screens
- Implement offline-first data persistence
- Add pause/resume download support
- Release beta on Play Store (internal testing)

**Q2 2026**
- Advanced filtering and search
- Batch processing improvements
- Accessibility enhancements
- Public Play Store release

**Q3 2026**
- Widget support
- Deep linking improvements
- Performance optimizations
- Advanced media player features

## Architecture Diagram

```
┌─────────────────────────────┐
│      Compose UI Screens     │
│  (InputScreen, InfoScreen,  │
│  DownloadScreen, HistScreen)│
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│      ViewModels             │
│ (VideoVM, DownloadVM,       │
│  HistoryVM)                 │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│   VideoRepository           │
│  (Result<T> wrapper)        │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│  SnapApiService (Retrofit)  │
│  (HTTP calls to backend)    │
└──────────────┬──────────────┘
               │
     ┌─────────┴─────────┐
     │                   │
┌────▼────┐        ┌────▼────┐
│ Network │        │ Serializ │
│(OkHttp) │        │(JSON)   │
└─────────┘        └─────────┘
```

## Next Steps

1. **Implement Input Screen** - URL entry and validation
2. **Implement Video Info Screen** - Display chapters with preview
3. **Implement Download Screen** - Progress bar and file completion
4. **Integrate Media Player** - Play downloaded media
5. **Setup Local Database** - Offline history storage
6. **Add Notifications** - Download completion alerts
7. **Testing** - Unit and integration tests
8. **Documentation** - API documentation and guides

---

**Last Updated**: February 2026
**Version**: 1.0.0-alpha
