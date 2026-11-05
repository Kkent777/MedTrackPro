# MedTrackPro

An Android portfolio application for medication schedules, symptom records and a clinician overview, built with **Kotlin, Jetpack Compose, ViewModels and Room**.

Developed by **Chenglin Li** as a university project. This public edition removes student-number identifiers and local configuration, and uses clearly labelled fictional demonstration records.

## Project highlights

- Compose screens and Navigation Compose for patient and clinician workflows.
- Session state and feature ViewModels separated from screen rendering.
- Room-backed repositories for medication, symptom and dashboard data.
- CSV import for a small, repeatable demonstration dataset.
- Optional Gemini integration with a locally configured development key.

## Open and run

1. Clone this repository and open its root folder in Android Studio.
2. Let Android Studio configure your Android SDK path and sync Gradle.
3. Install the SDK requested by the project and select an emulator or device running Android 8.0 / API 26 or later.
4. Run the `app` configuration.

The checked-in project uses Gradle 9.3.1, Android Gradle Plugin 9.1.0 and compile SDK 36.1. The Gradle wrapper is included. Use an Android Studio/JDK combination compatible with those versions.

`local.properties.example` documents local setup. Copy it to `local.properties` only if configuring paths manually. The real `local.properties`, signing keys and build outputs are ignored by Git.

For optional AI features, set your own `GEMINI_API_KEY` in the ignored `local.properties`. An empty value is the default. The development build places that value in `BuildConfig`; do not distribute an APK containing a personal or shared service key.

## Demonstration data

The CSV files under `app/src/main/assets` contain fictional records. Names are labelled **Demo Patient**, phone strings are local demonstration identifiers, and initial passwords are empty. Use the first-time account setup flow to claim a demo patient and set a password. These records are not real patients or clinical evidence.

This application is an educational prototype. Its local account flow stores demonstration passwords locally, and the clinician screen uses a fixed demonstration gate rather than server-enforced authorisation. Use fictional information only. Optional Gemini analysis sends the selected patient's display name, medication details and symptom context to the provider. This is not a production authentication system or medical advice.

## Code map

| Area | Purpose |
| --- | --- |
| `app/src/main/java/com/chenglin/medtrackpro/navigation` | App destinations and session state |
| `app/src/main/java/com/chenglin/medtrackpro/ui/screens` | Welcome, authentication and feature screens |
| `app/src/main/java/com/chenglin/medtrackpro/data` | Database, repositories and data import |
| `app/src/main/assets` | Fictional CSV seed data |
| `gradle/libs.versions.toml` | Dependency and plugin versions |

## Validation

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Building requires a matching local Android SDK. The repository keeps the original project toolchain rather than silently lowering its SDK or plugin requirements. The initial publication review checked 52 Kotlin files, internal references, XML resources and CSV relationships; the local build could not finish because that machine had no Android SDK. The GitHub Actions workflow supplies the requested SDK and runs the build and unit-test tasks. Runtime behaviour, device permissions and live provider responses require separate device checks.

SDK setup: [Android 16 QPR2 / API 36.1](https://developer.android.com/about/versions/16/qpr2/setup-sdk).

中文：MedTrackPro 展示 Android 页面导航、状态管理、Room 持久化和 CSV 导入等能力。公开版使用虚构演示资料，不包含原始课程文件、个人密钥或本机 SDK 路径。
