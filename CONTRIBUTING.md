# Contributing to Mahan Delivery Tracker

Thank you for considering contributing to Mahan! This document provides guidelines and instructions for contributing to the project.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Pull Request Process](#pull-request-process)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Features](#suggesting-features)

## 📜 Code of Conduct

We are committed to providing a welcoming and inspiring community for all. Please read and adhere to our Code of Conduct:

- 🤝 Be respectful and inclusive
- 🎯 Focus on what is best for the community
- 🙏 Show empathy towards other community members
- 💪 Be supportive and collaborative

## 🚀 Getting Started

### Prerequisites

- Android Studio 2023.1 or later
- JDK 11 or higher
- Git
- Android SDK API 34
- Gradle 8.0+

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/mahan.git
   cd mahan
   ```
3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/Mojigh47/mahan.git
   ```

## 🛠 Development Setup

### 1. Open Project in Android Studio

```bash
# Open with Android Studio
open -a "Android Studio" .
```

Or use File → Open in Android Studio.

### 2. Sync Gradle

```bash
./gradlew sync
```

### 3. Build and Run

```bash
# Build debug version
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Or run tests
./gradlew test
```

### 4. Set Up Git Hooks (Optional but Recommended)

```bash
# Copy pre-commit hook
cp .githooks/pre-commit .git/hooks/
chmod +x .git/hooks/pre-commit
```

## 📝 Making Changes

### Create a Feature Branch

```bash
# Update your local repository
git fetch upstream
git checkout main
git merge upstream/main

# Create a feature branch
git checkout -b feature/your-feature-name
```

Branch naming conventions:
- `feature/description` - New features
- `bugfix/description` - Bug fixes
- `docs/description` - Documentation
- `refactor/description` - Code refactoring
- `test/description` - Test improvements
- `chore/description` - Build, CI, dependencies

### Example

```bash
git checkout -b feature/enhanced-tracking-ui
git checkout -b bugfix/offline-sync-issue
git checkout -b docs/contributing-guide
```

## 🎨 Coding Standards

### Kotlin Style Guide

We follow the [Kotlin Official Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

### Key Guidelines

#### 1. Naming Conventions

```kotlin
// Classes and objects - PascalCase
class DeliveryTracker
object DeliveryManager
data class DeliveryData(...)

// Functions and variables - camelCase
fun trackDelivery() {}
var deliveryStatus = "pending"

// Constants - UPPER_SNAKE_CASE
const val MAX_RETRY_COUNT = 3
val DEFAULT_TIMEOUT = 5000L

// Private members - prefix with underscore (optional but recommended)
private val _deliveryFlow = MutableStateFlow<List<Delivery>>()
```

#### 2. Function Guidelines

```kotlin
// Prefer concise, clear functions
fun getDeliveryStatus(id: String): DeliveryStatus {
    return repository.getDeliveryById(id)?.status ?: DeliveryStatus.UNKNOWN
}

// Use extension functions for clarity
fun DeliveryData.isDelivered(): Boolean = this.status == "delivered"

// Prefer single expression functions when appropriate
fun getDeliveryCount() = deliveryList.size
```

#### 3. Null Safety

```kotlin
// Use safe calls
val status = delivery?.status

// Use Elvis operator for defaults
val statusDisplay = delivery?.status ?: "Unknown"

// Use let for non-null blocks
delivery?.let { 
    trackDelivery(it)
}

// Avoid !!
// ❌ DON'T: delivery!!.status
// ✅ DO: delivery?.status
```

#### 4. Coroutines Best Practices

```kotlin
// Use viewModelScope in ViewModels
viewModelScope.launch {
    try {
        val deliveries = repository.getDeliveries()
        _state.value = State.Success(deliveries)
    } catch (e: Exception) {
        _state.value = State.Error(e.message)
    }
}

// Use Flow for continuous data streams
fun getDeliveryFlow(): Flow<List<Delivery>> {
    return repository.getDeliveriesFlow()
}
```

#### 5. Compose Best Practices

```kotlin
// Use descriptive composable names
@Composable
fun DeliveryTrackingScreen(deliveryId: String) {
    // Implementation
}

// Keep composables small and focused
@Composable
fun DeliveryStatusBadge(status: DeliveryStatus) {
    // Small, focused composable
}

// Use remember for state
var selectedDelivery by remember { mutableStateOf<Delivery?>(null) }

// Preview functions
@Preview(showBackground = true)
@Composable
fun DeliveryTrackingScreenPreview() {
    MahanTheme {
        DeliveryTrackingScreen(deliveryId = "12345")
    }
}
```

### File Organization

```
src/main/java/moji/deliverytracker/
├── ui/
│   ├── screens/
│   │   ├── TrackingScreen.kt
│   │   ├── DeliveryDetailsScreen.kt
│   │   └── ProfileScreen.kt
│   ├── components/
│   │   ├── DeliveryCard.kt
│   │   ├── StatusBadge.kt
│   │   └── LoadingState.kt
│   └── theme/
│       ├── Theme.kt
│       ├── Color.kt
│       └── Typography.kt
├── viewmodel/
│   ├── TrackingViewModel.kt
│   └── DeliveryViewModel.kt
├── data/
│   ├── repository/
│   │   └── DeliveryRepository.kt
│   ├── local/
│   │   ├── DeliveryDao.kt
│   │   └── AppDatabase.kt
│   └── remote/
│       └── ApiService.kt
└── model/
    ├── Delivery.kt
    ├── DeliveryStatus.kt
    └── User.kt
```

## 🧪 Testing

### Unit Tests

```kotlin
// Example unit test
@Test
fun testDeliveryStatusUpdate() {
    val delivery = Delivery(id = "1", status = "pending")
    val updatedDelivery = delivery.copy(status = "in_transit")
    
    assertEquals("in_transit", updatedDelivery.status)
}
```

### Instrumentation Tests

```kotlin
// Example instrumentation test
@RunWith(AndroidJUnit4::class)
class DeliveryDatabaseTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Test
    fun testInsertAndRetrieveDelivery() {
        // Test implementation
    }
}
```

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests "moji.deliverytracker.DeliveryRepositoryTest"
```

## 📤 Submitting Changes

### Before You Submit

1. **Ensure your code follows standards** - Run code formatting
   ```bash
   ./gradlew ktlintFormat
   ```

2. **Run tests** - All tests must pass
   ```bash
   ./gradlew test
   ```

3. **Check for lint warnings**
   ```bash
   ./gradlew lint
   ```

4. **Update documentation** - Add/update relevant docs

5. **Update CHANGELOG.md** - Document your changes

### Commit Messages

Follow these guidelines for commit messages:

```
[Type]: Brief description (50 chars max)

Detailed explanation if needed. Wrap at 72 characters.
Reference issue if applicable: Fixes #123

Examples of [Type]:
- feat: New feature
- fix: Bug fix
- docs: Documentation
- style: Formatting
- refactor: Code reorganization
- test: Test additions
- chore: Dependencies, build config
```

#### Examples

```
feat: Add real-time delivery status updates

Implement WebSocket connection for live delivery status
tracking. Includes automatic reconnection and offline
fallback.

Fixes #45
```

```
fix: Resolve offline sync race condition

Prevent duplicate entries when syncing offline data
by adding unique constraint checks before insertion.

Fixes #78
```

## 🔄 Pull Request Process

### Creating a Pull Request

1. **Push your branch**
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create PR on GitHub**
   - Go to your fork
   - Click "Compare & pull request"
   - Fill in the PR template

3. **PR Title Format**
   ```
   [TYPE] Brief description
   
   Examples:
   [FEATURE] Add real-time tracking notifications
   [BUGFIX] Fix offline sync race condition
   [DOCS] Update README with new features
   ```

4. **PR Description Template**
   ```markdown
   ## Description
   Brief description of changes
   
   ## Type of Change
   - [ ] New feature
   - [ ] Bug fix
   - [ ] Breaking change
   - [ ] Documentation
   
   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Instrumentation tests added/updated
   - [ ] Manual testing completed
   
   ## Screenshots (if applicable)
   
   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Documentation updated
   - [ ] Tests pass
   - [ ] No new warnings generated
   
   ## Related Issues
   Fixes #123
   ```

### Review Process

1. **Address feedback** - Respond to reviewer comments
2. **Make requested changes** - Push updates to your branch
3. **Request re-review** - Ask for another review after changes
4. **Squash if needed** - Clean up commit history if necessary

### Merging

Once approved:
- Maintainers will merge your PR
- Your branch will be deleted
- Changes will be included in the next release

## 🐛 Reporting Bugs

### Before Reporting

1. Check existing issues
2. Search closed issues for similar problems
3. Check the [FAQ](README.md#getting-help)

### Bug Report Template

```markdown
## Description
Brief description of the bug

## Steps to Reproduce
1. Step 1
2. Step 2
3. Step 3

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- Device: (e.g., Pixel 6)
- OS Version: (e.g., Android 14)
- App Version: (e.g., 2.0)

## Screenshots/Logs
Include relevant screenshots or error logs

## Additional Context
Any other relevant information
```

## 💡 Suggesting Features

### Feature Request Template

```markdown
## Description
What feature would you like to see?

## Problem Solved
What problem does this solve?

## Proposed Solution
How should this feature work?

## Alternatives Considered
Other approaches you've considered

## Additional Context
Any other relevant information
```

## 📚 Additional Resources

- [Android Developer Documentation](https://developer.android.com)
- [Kotlin Documentation](https://kotlinlang.org/docs)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Architecture Components Guide](https://developer.android.com/topic/libraries/architecture)

## ❓ Questions?

- 📖 Check the README
- 💬 Open a Discussion
- 📧 Contact maintainers

---

## Recognition

Contributors will be recognized in:
- GitHub contributors page
- CHANGELOG.md file
- Release announcements

Thank you for contributing to Mahan! 🙏
