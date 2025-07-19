# Nexus Controller Hub - Setup Instructions

## 🚀 Quick Start Guide

### Prerequisites
Before building the Nexus Controller Hub, ensure you have the following installed:

1. **Android Studio** (Arctic Fox or later)
   - Download from: https://developer.android.com/studio
   - Includes Android SDK, build tools, and emulator

2. **Java Development Kit (JDK) 17+**
   - Android Studio includes OpenJDK
   - Or install separately: https://adoptium.net/

3. **Android SDK Components**
   - Android SDK Platform 34 (API level 34)
   - Android SDK Build-Tools 34.0.0
   - Android Support Repository

### 📱 Device Requirements
- **Minimum Android Version**: Android 5.0 (API level 21)
- **Target Android Version**: Android 14 (API level 34)
- **Architecture**: ARM64, ARM32, x86_64 (universal APK)
- **RAM**: 2GB minimum, 4GB recommended
- **Storage**: 50MB for app installation

## 🛠️ Development Setup

### 1. Clone and Open Project
```bash
# Clone the repository
git clone <repository-url>
cd nexus-controller-hub/android

# Open in Android Studio
# File → Open → Select the 'android' folder
```

### 2. SDK Configuration
Android Studio will automatically prompt to install missing SDK components. If not:

1. Open **Tools → SDK Manager**
2. Install the following:
   - **SDK Platforms**: Android 14.0 (API 34)
   - **SDK Tools**: 
     - Android SDK Build-Tools 34.0.0
     - Android Emulator
     - Android SDK Platform-Tools

### 3. Gradle Sync
1. Android Studio will automatically sync Gradle
2. If issues occur, click **File → Sync Project with Gradle Files**
3. Wait for dependencies to download (first time may take 5-10 minutes)

### 4. Build Configuration
The project includes multiple build variants:
- **debug**: Development build with debugging enabled
- **release**: Production build with optimizations

## 🔧 Build Instructions

### Debug Build (Development)
```bash
# Command line build
./gradlew assembleDebug

# Or in Android Studio
Build → Make Project (Ctrl+F9)
```

### Release Build (Production)
```bash
# Command line build
./gradlew assembleRelease

# Or in Android Studio
Build → Generate Signed Bundle/APK
```

### Install on Device
```bash
# Install debug APK
./gradlew installDebug

# Or use ADB directly
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📱 Testing Setup

### Physical Device Testing
1. **Enable Developer Options**:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Return to Settings → Developer Options

2. **Enable USB Debugging**:
   - In Developer Options, enable "USB Debugging"
   - Connect device via USB
   - Accept debugging authorization prompt

3. **Connect Controller**:
   - Pair Bluetooth controller or connect USB controller
   - Verify controller works in other apps

### Emulator Testing
1. **Create AVD** (Android Virtual Device):
   - Tools → AVD Manager → Create Virtual Device
   - Choose device (Pixel 6 recommended)
   - Select API 34 system image
   - Configure hardware (enable hardware keyboard)

2. **Controller Emulation**:
   - Use physical controller connected to computer
   - Or use keyboard mapping for testing

## 🔐 Permissions Setup

### Required Permissions
The app requires the following permissions:

1. **Accessibility Service** (Critical):
   - Allows global input interception
   - User must manually enable in Settings
   - App provides guided setup flow

2. **File Access** (Optional):
   - For profile import/export functionality
   - Requested only when needed

### Accessibility Service Setup
1. Install and launch the app
2. Follow onboarding flow
3. When prompted, tap "Enable Accessibility Service"
4. Navigate to: Settings → Accessibility → Downloaded Apps
5. Find "Nexus Controller Hub" and enable it
6. Return to app to verify activation

## 🧪 Testing Checklist

### Basic Functionality
- [ ] App launches without crashes
- [ ] Onboarding flow completes successfully
- [ ] Accessibility service can be enabled
- [ ] Controller detection works
- [ ] Profile creation and editing
- [ ] Button remapping functions
- [ ] Analog calibration responds to input
- [ ] Macro recording and playback
- [ ] Profile import/export

### Advanced Testing
- [ ] Multiple controller support
- [ ] Profile switching while gaming
- [ ] Macro execution in different apps
- [ ] Service survives app closure
- [ ] Battery optimization handling
- [ ] Memory usage under load

## 🐛 Troubleshooting

### Common Issues

#### Build Errors
**Problem**: "SDK location not found"
**Solution**: 
```bash
# Create local.properties file
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
```

**Problem**: "Gradle sync failed"
**Solution**:
1. Check internet connection
2. Clear Gradle cache: `./gradlew clean`
3. Restart Android Studio

#### Runtime Issues
**Problem**: "Accessibility service not working"
**Solution**:
1. Verify service is enabled in Settings
2. Restart the app
3. Check device compatibility

**Problem**: "Controller not detected"
**Solution**:
1. Verify controller works in other apps
2. Check USB/Bluetooth connection
3. Try different controller if available

#### Performance Issues
**Problem**: "Input lag or missed inputs"
**Solution**:
1. Close other apps to free memory
2. Disable battery optimization for the app
3. Check for system updates

### Debug Tools

#### Logging
Enable verbose logging in debug builds:
```kotlin
// In ControllerAccessibilityService
Log.d("ControllerHub", "Input event: $event")
```

#### ADB Commands
```bash
# View app logs
adb logcat | grep ControllerHub

# Check accessibility services
adb shell settings get secure enabled_accessibility_services

# Force stop app
adb shell am force-stop com.nexus.controllerhub
```

## 📊 Performance Optimization

### Build Optimization
1. **Enable R8/ProGuard** for release builds
2. **Use APK Analyzer** to check size
3. **Enable multidex** if needed
4. **Optimize resources** with vector drawables

### Runtime Optimization
1. **Battery Optimization**: Request exemption for background service
2. **Memory Management**: Monitor heap usage during development
3. **Input Latency**: Profile accessibility service performance
4. **Storage**: Implement database cleanup for old profiles

## 🚀 Deployment

### Google Play Store
1. **Create signed release build**
2. **Test on multiple devices**
3. **Prepare store listing**:
   - Screenshots from different devices
   - Feature descriptions
   - Privacy policy
4. **Upload to Play Console**

### Alternative Distribution
1. **Direct APK**: Share release APK file
2. **F-Droid**: Open source app store
3. **Enterprise**: Internal distribution

## 📋 Development Workflow

### Recommended Development Process
1. **Feature Branch**: Create branch for new features
2. **Local Testing**: Test on emulator and physical device
3. **Code Review**: Review changes before merging
4. **Integration Testing**: Test with full app flow
5. **Release Testing**: Test release build before deployment

### Version Management
```kotlin
// In app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### Continuous Integration
Consider setting up CI/CD with:
- **GitHub Actions** for automated builds
- **Firebase App Distribution** for beta testing
- **Automated testing** on multiple devices

## 📞 Support

### Getting Help
- **Documentation**: Check README.md for detailed information
- **Issues**: Report bugs and feature requests on GitHub
- **Community**: Join discussions for tips and tricks

### Contributing
1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request
5. Follow code review process

---

**Happy coding! The Nexus Controller Hub is ready for development and deployment.** 🎮