# Android Implementation Guide

## Overview

This guide provides step-by-step instructions for implementing the Snap Android app. The codebase is organized in phases, starting with the core API integration and data layer (completed), followed by UI screens and features.

## Current Implementation Status

### ✅ Completed (Phase 1: Core Infrastructure)

1. **Project Structure**
   - Gradle configuration (build.gradle.kts, settings.gradle.kts)
   - Module organization (app module)
   - Dependency management (gradle.properties)

2. **Data Models** (`data/models/VideoModels.kt`)
   - `VideoInfo` - Video metadata
   - `Chapter` - Chapter information with time formatting
   - Download enums (Quality, Mode, Format)
   - `DownloadProgress` - Real-time progress data
   - `DownloadHistoryEntity` & `DownloadHistory` - History models
   - API response models (VideoInfoResponse, DownloadStatusResponse, etc.)

3. **API Integration** (`data/api/`)
   - `SnapApiService` - Retrofit interface with all 7 endpoints
   - Request models (VideoInfoRequest, DownloadRequest)
   - `ApiServiceFactory` - Factory for creating configured Retrofit instances
   - Proper error handling and logging configuration

4. **Repository Pattern** (`data/repository/VideoRepository.kt`)
   - `Result<T>` sealed class for type-safe error handling
   - Suspend functions for API calls
   - Flow-based progress monitoring
   - Error recovery and retry logic

5. **ViewModels** (`ui/viewmodel/`)
   - `VideoViewModel` - Video info and chapter selection state
   - `DownloadViewModel` - Download progress monitoring
   - `HistoryViewModel` - History loading and clearing
   - Proper state management with StateFlow

6. **App Initialization**
   - `SnapApplication` - Application class with repository injection
   - `MainActivity` - Main activity with Compose setup
   - Theme configuration with Material Design 3

### 🚀 Next Phase: UI Screens (Phase 2)

The following screens need to be implemented in `ui/screen/`:

#### 1. InputScreen
- **Purpose**: Allow users to enter video URLs
- **Features**:
  - Single URL input field with validation
  - Multi-URL textarea (one URL per line)
  - Mode toggle button (Single/Multiple)
  - Fetch button to retrieve video info
  - Error messages with retry

**Implementation Outline**:
```kotlin
package com.snap.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snap.ui.viewmodel.VideoViewModel

@Composable
fun InputScreen(
    viewModel: VideoViewModel,
    onVideoInfoLoaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("") }
    var inputMode by remember { mutableStateOf(InputMode.SINGLE) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Mode toggle buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = { inputMode = InputMode.SINGLE },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("1 Link")
            }
            Button(
                onClick = { inputMode = InputMode.MULTIPLE },
                modifier = Modifier.weight(1f)
            ) {
                Text("Múltiplos")
            }
        }
        
        // Input field based on mode
        when (inputMode) {
            InputMode.SINGLE -> {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL do vídeo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
            InputMode.MULTIPLE -> {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URLs (uma por linha)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(bottom = 16.dp),
                    maxLines = 10
                )
            }
        }
        
        // Fetch button
        Button(
            onClick = {
                viewModel.fetchVideoInfo(url)
                onVideoInfoLoaded()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = url.isNotBlank() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Buscar Informações")
            }
        }
        
        // Error message
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

enum class InputMode {
    SINGLE, MULTIPLE
}
```

#### 2. VideoInfoScreen
- **Purpose**: Display video metadata and chapter selection
- **Features**:
  - Thumbnail display
  - Video title and duration
  - Site badge with icon
  - Chapter list with checkboxes
  - Format and quality selection
  - Download mode toggle (Single/Separated)
  - Download button

**Key Components**:
- Video card with thumbnail and metadata
- Chapters list with select all/deselect all buttons
- Format dropdown (MP4, MKV, MP3, etc.)
- Quality dropdown (Best, 2160p, 1440p, 1080p, 720p, 480p, 360p)
- Download mode radio buttons

#### 3. DownloadScreen
- **Purpose**: Show download progress
- **Features**:
  - Progress bar with percentage
  - File size display (current/total)
  - Download speed
  - Status messages
  - Cancel button (if supported)
  - Open file button (after completion)

#### 4. HistoryScreen
- **Purpose**: Display and manage download history
- **Features**:
  - Scrollable list of downloads
  - Date and time for each
  - File name and format
  - Delete individual entries
  - Clear all button
  - Search/filter capability (optional)

## Implementation Priority

### Priority 1: Core Screens (Week 1)
1. Complete InputScreen with validation
2. Complete VideoInfoScreen with chapter display
3. Navigation between screens

### Priority 2: Download Management (Week 2)
1. Implement DownloadViewModel progress polling
2. Complete DownloadScreen with progress visualization
3. Download completion handling

### Priority 3: Local Storage (Week 3)
1. Implement Room database integration
2. History persistence
3. Offline access to history

### Priority 4: Polish & Testing (Week 4)
1. Error handling improvements
2. Loading states
3. Empty states
4. Unit tests

## File Creation Checklist

### Screen Files to Create
- [ ] `ui/screen/InputScreen.kt`
- [ ] `ui/screen/VideoInfoScreen.kt`
- [ ] `ui/screen/DownloadScreen.kt`
- [ ] `ui/screen/HistoryScreen.kt`
- [ ] `ui/screen/components/VideoCard.kt`
- [ ] `ui/screen/components/ChapterList.kt`
- [ ] `ui/screen/components/DownloadOptions.kt`

### Navigation
- [ ] `ui/navigation/SnapNavGraph.kt`
- [ ] `ui/navigation/Screen.kt` (sealed class for routes)

### Database (Optional for Phase 1)
- [ ] `data/database/SnapDatabase.kt`
- [ ] `data/database/dao/DownloadHistoryDao.kt`

### UI Components
- [ ] `ui/components/LoadingDialog.kt`
- [ ] `ui/components/ErrorDialog.kt`
- [ ] `ui/components/SiteBadge.kt`
- [ ] `ui/components/ProgressIndicator.kt`

## Testing Files to Create
- [ ] `test/kotlin/com/snap/ui/screen/InputScreenTest.kt`
- [ ] `test/kotlin/com/snap/ui/screen/VideoInfoScreenTest.kt`
- [ ] `test/kotlin/com/snap/ui/viewmodel/VideoViewModelTest.kt`
- [ ] `test/kotlin/com/snap/ui/viewmodel/DownloadViewModelTest.kt`

## Configuration Steps

### 1. Backend Connection
In `ApiServiceFactory.kt`:
```kotlin
// For local development
private const val DEFAULT_BASE_URL = "http://192.168.1.100:5000"

// For emulator (host machine)
private const val DEFAULT_BASE_URL = "http://10.0.2.2:5000"

// For production
private const val DEFAULT_BASE_URL = "https://snap.example.com"
```

### 2. Gradle Configuration
Update `gradle.properties`:
```properties
# Adjust SDK versions if needed
sdk.compile = 34
sdk.min = 29
sdk.target = 34

# Kotlin version
kotlin.version = 1.9.22

# Compose version
compose.version = 1.6.0
```

### 3. AndroidManifest.xml
Already configured with:
- Network permissions
- Storage permissions (for downloads)
- Notification permission (for alerts)

## Running the App

### In Android Studio
1. Open Project → `android/`
2. Sync Gradle
3. Click "Run" or press Shift+F10
4. Select emulator or device

### From Command Line
```bash
# Build debug APK
./gradlew assembleDebug

# Install on device/emulator
./gradlew installDebug

# Run on device
./gradlew installDebugAndroidTest
```

## Debugging Tips

### Logcat Filtering
```bash
# Filter by app package
adb logcat | grep com.snap

# Filter by tag
adb logcat *:E  # Errors only
```

### Common Issues

**Issue: Connection refused (API connection)**
- Check if backend is running: `http://localhost:5000`
- For emulator, use `http://10.0.2.2:5000`
- Check firewall settings

**Issue: ClassCastException in ViewModel**
- Ensure ViewModels are created with correct factory
- Check that repository is properly initialized

**Issue: Compose compilation error**
- Clean build: `./gradlew clean build`
- Invalidate caches in Android Studio

## Integration Checkpoints

1. **API Service** ✅ (Completed)
   - [ ] Verify all 7 endpoints are callable
   - [ ] Test with curl/Postman

2. **Repository** ✅ (Completed)
   - [ ] Test fetchVideoInfo() locally
   - [ ] Test error handling

3. **ViewModels** ✅ (Completed)
   - [ ] Verify state updates correctly
   - [ ] Test with LiveData inspector

4. **Screens** 🔄 (In Progress)
   - [ ] InputScreen compiles and displays
   - [ ] Navigation works between screens

5. **End-to-End** (Next)
   - [ ] Enter URL → Fetch info → Display
   - [ ] Select options → Download → Monitor progress

## Code Style & Conventions

### Kotlin Conventions
- Use `val` by default, only use `var` when needed
- Use `data class` for models
- Use `sealed class` for type-safe results
- Use `@Composable` functions for UI

### Naming Conventions
- Screens: `XxxScreen.kt`
- ViewModels: `XxxViewModel.kt`
- Models: `Xxx.kt` (plural for lists)
- Repositories: `XxxRepository.kt`
- Services: `XxxService.kt`

### File Organization
- Keep one public class per file
- Group related code with comments
- Use package structure to organize features
- Private utilities at bottom of file

## Documentation

### Code Comments
```kotlin
// Public API should have KDoc
/**
 * Fetches video information from the given URL.
 *
 * @param url The video URL
 * @return Result<VideoInfo> with success or error
 */
suspend fun getVideoInfo(url: String): Result<VideoInfo>
```

### Commit Messages
```
feat: Add InputScreen with URL validation
fix: Fix progress percentage display in DownloadScreen
docs: Update README with setup instructions
refactor: Move theme colors to separate file
test: Add VideoViewModelTest
```

## Next Steps After UI Implementation

1. **Add Database Support**
   - Implement Room entities
   - Create DAOs
   - Add migrations

2. **Add Notifications**
   - Show download completion
   - Add progress notifications
   - Handle notification permissions

3. **Add Offline Support**
   - Cache downloaded files
   - Sync when online
   - Offline history access

4. **Performance Optimization**
   - Image loading with Coil
   - Pagination for history
   - Lazy loading for chapters

5. **Release Preparation**
   - Signing key setup
   - Version management
   - Play Store configuration

---

**Last Updated**: February 2026
**Status**: Phase 1 Complete, Phase 2 Ready to Start
