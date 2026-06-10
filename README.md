# 📱 Mahan Delivery Tracker

A modern Android delivery tracking application built with Kotlin and Jetpack Compose.

## ✨ Features

- **Real-time Tracking**: Track delivery status in real-time
- **Modern UI**: Built with Material Design 3
- **Offline Support**: Room Database for offline functionality
- **Coroutines**: Asynchronous programming with Kotlin Coroutines
- **Type-Safe**: 100% Kotlin implementation

## 🛠 Tech Stack

- **Language**: Kotlin 1.9.10
- **Build System**: Gradle
- **Target API**: 34 (Android 14)
- **Min API**: 24 (Android 7.0)
- **Architecture**: MVVM with Room Database

### Core Dependencies

- AndroidX Core & AppCompat
- Material Design 3
- Room Database
- Kotlin Coroutines
- Lifecycle Components
- RecyclerView & CardView
- Shimmer Effects

## 🚀 Quick Start

### Prerequisites

- Android Studio 2023.1+
- JDK 11+
- Android SDK 34

### Installation

```bash
git clone https://github.com/Mojigh47/mahan.git
cd mahan
# Open in Android Studio and sync Gradle
```

## 📦 Version Info

- **versionCode**: 2
- **versionName**: 2.0
- **applicationId**: moji.deliverytracker
- **compileSdk**: 34
- **targetSdk**: 34
- **minSdk**: 24

## 🔐 Release Build

Create `app/keystore.properties`:
```properties
storeFile=path/to/keystore.jks
storePassword=password
keyAlias=alias
keyPassword=password
```

Build release APK:
```bash
./gradlew assembleRelease
```

## 📄 License

MIT License - See LICENSE file for details

## 👤 Author

**Mojigh47** - [GitHub](https://github.com/Mojigh47)

---

**Status**: ✅ Production Ready (v2.0)
