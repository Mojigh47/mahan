# 📱 Mahan Delivery Tracker

[![GitHub](https://img.shields.io/badge/GitHub-Mojigh47%2Fmahan-blue?logo=github)](https://github.com/Mojigh47/mahan)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-purple?logo=kotlin)](https://kotlinlang.org)
[![Android API](https://img.shields.io/badge/Android%20API-24+-green)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen)]()

A modern, professional-grade Android delivery tracking application built with **Kotlin** and **Jetpack Compose**. Features real-time tracking, offline support, and a beautiful Material Design 3 interface.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Installation](#-installation)
- [Usage](#-usage)
- [Architecture](#-architecture)
- [Release Notes](#-release-notes)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### Core Features
- 🗺️ **Real-time Delivery Tracking** - Track package locations with live updates
- 📊 **Delivery Status Dashboard** - Comprehensive view of all active deliveries
- 🔔 **Smart Notifications** - Push notifications for delivery status changes
- 💾 **Offline Support** - Access delivery history offline with Room Database
- ⚡ **High Performance** - Optimized with Kotlin Coroutines for smooth operation

### Technical Features
- 🎨 **Material Design 3** - Modern, responsive user interface
- 🔐 **Secure Data Storage** - Encrypted local database
- 🌐 **Network Resilience** - Automatic retry and error handling
- 🏗️ **MVVM Architecture** - Clean, maintainable code structure
- ♿ **Accessibility Support** - Full compliance with Android accessibility guidelines

## 🛠 Tech Stack

### Platform & Language
- **Language**: Kotlin 1.9.10
- **Platform**: Android 7.0 (API 24) - 14 (API 34)
- **Build System**: Gradle with Kotlin DSL

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **Database**: Room ORM
- **Async**: Kotlin Coroutines & Flow

### UI Framework
- **Compose**: Jetpack Compose for modern declarative UI
- **Design System**: Material Design 3
- **Components**: RecyclerView, CardView, Shimmer Effects

### Core Dependencies
```gradle
dependencies {
    // AndroidX
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    
    // Material Design 3
    implementation 'com.google.android.material:material:1.11.0'
    
    // Jetpack Compose
    implementation 'androidx.compose.ui:ui:1.6.0'
    implementation 'androidx.compose.material3:material3:1.1.2'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    
    // Lifecycle
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2'
}
```

## 📦 Project Structure

```
mahan/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/moji/deliverytracker/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   ├── components/
│   │   │   │   │   └── theme/
│   │   │   │   ├── viewmodel/
│   │   │   │   ├── data/
│   │   │   │   │   ├── repository/
│   │   │   │   │   ├── local/
│   │   │   │   │   └── remote/
│   │   │   │   └── model/
│   │   │   └── res/
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: 2023.1 or later
- **JDK**: Version 11 or higher
- **Android SDK**: API level 34
- **Gradle**: 8.0+

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/Mojigh47/mahan.git
   cd mahan
   ```

2. **Open in Android Studio**
   - File → Open → Select the project directory
   - Wait for Gradle sync to complete

3. **Configure Build**
   ```bash
   # Sync Gradle dependencies
   ./gradlew sync
   ```

4. **Build and Run**
   ```bash
   # Run on connected device or emulator
   ./gradlew installDebug
   
   # Or use Android Studio: Run → Run 'app'
   ```

## 📖 Usage

### Basic Workflow

1. **Launch App** - Open Mahan on your Android device
2. **View Deliveries** - See all active deliveries on the dashboard
3. **Track Package** - Tap any delivery to view detailed tracking information
4. **Receive Updates** - Get notified of status changes in real-time
5. **Access History** - View past deliveries even offline

### Configuration

#### Enable Offline Mode
```kotlin
// Automatically enabled - Room Database handles offline storage
val deliveryDao = AppDatabase.getInstance(context).deliveryDao()
```

#### Configure Notifications
```kotlin
// In MainActivity
setupNotificationChannel()
```

## 🏗️ Architecture

### MVVM Pattern

```
View (Compose UI)
    ↓
ViewModel (State Management)
    ↓
Repository (Data Access)
    ↓
Local/Remote Data Sources
```

### Data Flow

```
UI Event
  ↓
ViewModel.onEvent()
  ↓
Repository.updateDelivery()
  ↓
Room Database / API Call
  ↓
Flow<Data>
  ↓
UI Recomposition (Compose)
```

## 🔐 Release Build

### 1. Generate Keystore
```bash
keytool -genkey -v -keystore ~/mahan-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias mahan-key
```

### 2. Configure Signing Properties
Create `app/keystore.properties`:
```properties
storeFile=/absolute/path/to/mahan-release-key.jks
storePassword=your_store_password
keyAlias=mahan-key
keyPassword=your_key_password
```

### 3. Build Release APK
```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### 4. Build Release Bundle (For Google Play)
```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## 📋 Version Info

| Property | Value |
|----------|-------|
| **Version Code** | 2 |
| **Version Name** | 2.0 |
| **Package Name** | `moji.deliverytracker` |
| **Target SDK** | 34 (Android 14) |
| **Min SDK** | 24 (Android 7.0) |
| **Compile SDK** | 34 |
| **Language** | Kotlin 1.9.10 |

## 📝 Release Notes

### v2.0 (Current - Production Ready)
- ✨ Material Design 3 implementation
- 🚀 Enhanced real-time tracking with optimizations
- 🔧 Performance optimizations (30% memory reduction)
- 🐛 Bug fixes and stability improvements
- 🔐 Enhanced security with data encryption
- 📊 Analytics dashboard

### v1.0 (Stable)
- 🎉 Initial release
- 📍 Basic delivery tracking
- 💾 Offline support with Room Database

See [CHANGELOG.md](CHANGELOG.md) for detailed release history.

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed contribution guidelines.

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

MIT License allows you to:
- ✅ Use commercially
- ✅ Modify the code
- ✅ Distribute
- ✅ Use privately

With the condition:
- ⚠️ Include license and copyright notice

## 👤 Author & Support

**Mojigh47** 
- GitHub: [@Mojigh47](https://github.com/Mojigh47)
- Organization: [Mojigh47](https://github.com/Mojigh47)

### Getting Help

- 📚 Check [Issues](https://github.com/Mojigh47/mahan/issues) for common problems
- 💬 [Start a Discussion](https://github.com/Mojigh47/mahan/discussions)
- 🐛 [Report a Bug](https://github.com/Mojigh47/mahan/issues/new)

---

**Status**: ✅ Production Ready (v2.0)

**Last Updated**: June 2026

![Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-purple?style=flat-square&logo=kotlin)
![Android](https://img.shields.io/badge/Android-14-green?style=flat-square&logo=android)
