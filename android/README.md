# Nexus Controller Hub

A comprehensive, offline-first Android application for controller customization, button remapping, analog calibration, and macro recording.

## 🎮 Features

### Core Functionality
- **Universal Controller Support**: Works with any Android-compatible controller (Bluetooth/USB)
- **Button Remapping**: Intuitive tap-to-assign system for customizing button layouts
- **Analog Calibration**: Dead zone adjustment, sensitivity curves, and trigger actuation points
- **Macro Recording**: Record, edit, and assign complex input sequences
- **Profile Management**: Create, save, and switch between multiple controller configurations
- **Offline-First**: All data stored locally using Room database - no internet required

### User Experience
- **Material 3 Design**: Modern, responsive UI built with Jetpack Compose
- **Guided Onboarding**: Step-by-step setup for accessibility service permissions
- **Real-time Feedback**: Visual controller representation with live input visualization
- **Import/Export**: Backup and share profiles as JSON files

## 🏗️ Architecture

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Database**: Room (SQLite)
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Manual DI with ViewModelFactory
- **Serialization**: Kotlinx Serialization
- **Navigation**: Jetpack Navigation Compose

### Core Components

#### Data Layer
- **Room Database**: Persistent storage for profiles and macros
- **Entities**: `ControllerProfile`, `Macro`, `MacroAction`, `AnalogSettings`
- **DAOs**: Type-safe database access with Flow-based reactive queries
- **Repository**: Centralized data access with caching and business logic

#### Service Layer
- **ControllerAccessibilityService**: Global input interception and processing
- **ControllerInputProcessor**: Real-time button remapping and analog calibration
- **MacroPlayer**: Precise macro playback with timing control

#### UI Layer
- **Screens**: Onboarding, Dashboard, Profiles, Configuration, Macros, Macro Editor
- **ViewModels**: State management with StateFlow and coroutines
- **Components**: Reusable UI components including controller visualization

#### Utility Layer
- **ControllerDetector**: Automatic controller detection and monitoring
- **FileManager**: Profile import/export with FileProvider integration
- **PermissionHelper**: Accessibility service management utilities

## 📱 User Journey

### 1. First Launch & Setup
- Welcome screen with app overview
- Accessibility service permission request with clear explanation
- Direct navigation to system settings
- Verification of service activation before proceeding

### 2. Main Dashboard
- Real-time controller connection status
- Active profile display with quick edit access
- Quick action cards for common tasks
- Accessibility service status monitoring

### 3. Profile Management
- Create, rename, duplicate, and delete profiles
- Visual profile cards with metadata
- One-tap profile activation
- Import/export functionality for backup and sharing

### 4. Controller Configuration
- **Button Mapping Tab**: Visual controller with tap-to-remap functionality
- **Calibration Tab**: Analog stick and trigger fine-tuning with sliders
- **Macro Assignment Tab**: Assign recorded macros to buttons or combinations

### 5. Macro System
- **Recording**: Start/stop recording with visual feedback
- **Editing**: Step-by-step macro editor with timing adjustment
- **Testing**: One-tap macro testing for verification
- **Assignment**: Flexible assignment to single buttons, combinations, or long-press

## 🔧 Technical Implementation

### Accessibility Service Integration
The app uses Android's Accessibility Service framework to:
- Intercept controller input events globally
- Apply real-time button remapping
- Execute analog stick calibration
- Trigger macro playback
- Work across all applications without root access

### Real-time Input Processing
```kotlin
// Example of input processing pipeline
InputEvent → ControllerAccessibilityService → ControllerInputProcessor → 
Modified Event → System Dispatch
```

### Database Schema
```kotlin
@Entity
data class ControllerProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val buttonMappings: Map<String, String>,
    val analogSettings: AnalogSettings,
    val macroAssignments: Map<String, Long>,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
```

### State Management
- **StateFlow**: Reactive state management in ViewModels
- **Flow**: Database queries with automatic UI updates
- **Coroutines**: Asynchronous operations and background processing

## 🚀 Setup & Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 21+ (Android 5.0+)
- Kotlin 1.9.10+
- Gradle 8.4+

### Build Instructions
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Build and run on device or emulator

### Required Permissions
- `BIND_ACCESSIBILITY_SERVICE`: Core functionality for input interception
- `SYSTEM_ALERT_WINDOW`: Overlay permissions for advanced features
- File access permissions for import/export functionality

## 📋 Project Structure

```
app/src/main/java/com/nexus/controllerhub/
├── data/
│   ├── database/          # Room database setup
│   ├── model/            # Data models and entities
│   └── repository/       # Repository pattern implementation
├── service/              # Accessibility service and input processing
├── ui/
│   ├── component/        # Reusable UI components
│   ├── screen/          # Screen composables
│   ├── theme/           # Material 3 theming
│   └── viewmodel/       # ViewModels and state management
├── util/                # Utility classes and helpers
└── MainActivity.kt      # Main entry point
```

## 🎯 Key Features Implementation

### Button Remapping
- Visual controller representation with clickable buttons
- Modal dialog for selecting target button or key event
- Real-time mapping preview and validation
- Support for complex key combinations

### Analog Calibration
- Inner/outer dead zone sliders with live preview
- Sensitivity curve editor with visual feedback
- Per-stick configuration (left/right independent)
- Trigger actuation point customization

### Macro System
- High-precision timing capture (millisecond accuracy)
- Step-by-step editor with individual action modification
- Support for button presses, releases, and analog movements
- Flexible assignment system (single button, combinations, long-press)

### Profile Management
- JSON-based import/export for cross-device compatibility
- Profile metadata tracking (creation/modification dates)
- Automatic backup suggestions
- Quick profile switching with visual feedback

## 🔒 Security & Privacy

- **Local-Only Storage**: All data remains on device
- **No Network Access**: App functions completely offline
- **Minimal Permissions**: Only requests necessary permissions
- **Accessibility Service**: Clearly explained purpose and usage
- **Data Encryption**: Sensitive settings can be encrypted at rest

## 🧪 Testing Strategy

### Unit Tests
- Repository layer with mock database
- ViewModel state management
- Input processing algorithms
- Macro timing accuracy

### Integration Tests
- Database operations with Room testing
- Accessibility service integration
- File import/export functionality

### UI Tests
- Navigation flow testing
- User interaction scenarios
- Accessibility compliance

## 📈 Performance Considerations

- **Efficient Input Processing**: Minimal latency for real-time remapping
- **Memory Management**: Proper lifecycle handling for services
- **Battery Optimization**: Intelligent service management
- **Database Optimization**: Indexed queries and efficient schemas

## 🔮 Future Enhancements

- **Cloud Sync**: Optional profile synchronization
- **Community Profiles**: Share and discover configurations
- **Advanced Macros**: Conditional logic and variables
- **Gesture Recognition**: Custom gesture-to-action mapping
- **Multiple Controller Support**: Simultaneous controller management
- **Game-Specific Profiles**: Automatic profile switching per app

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please read the contributing guidelines and submit pull requests for any improvements.

## 📞 Support

For issues, feature requests, or questions, please open an issue on the GitHub repository.

---

**Nexus Controller Hub** - Unleash your controller's full potential with precision customization and macro automation.