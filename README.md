# LiftLog

LiftLog is an offline-first Android app for tracking gym progress.

## Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM with lightweight Clean Architecture
- Room 3 with FTS search
- Kotlin Coroutines and Flow
- Hilt
- Navigation Compose
- DataStore

## Current scope

This repository starts with a production-oriented project skeleton:

- app shell with dark Material 3 theme,
- bottom navigation placeholders,
- searchable exercise list,
- Room entities, DAO and repository,
- use cases for exercise loading and starter data,
- Hilt wiring,
- DataStore settings repository,
- one pure unit test example.

## Local setup

1. Install Android Studio.
2. Open the cloned project folder in Android Studio.

3. Let Android Studio install the required Android SDK, Gradle and JDK.
4. Run the `app` configuration on an emulator or physical device.

The project is intentionally offline-first. Cloud sync can be added later behind repository interfaces without rewriting the UI layer.
