# MedTrackPro

An Android app for tracking medication schedules and symptoms, with a clinician dashboard for reviewing patient records. Built with Kotlin, Jetpack Compose and Room. The included [CSV records](app/src/main/assets) are fictional.

## Run

1. Open the project in Android Studio, use JDK 21 and install Android SDK 36.1.
2. Sync Gradle and run the `app` configuration on an emulator or device with Android 8.0 (API 26) or later.
3. Select first-time setup and enter a patient ID and phone number from [patients.csv](app/src/main/assets/patients.csv) to set a demo password.

For a command-line build, run `./gradlew :app:assembleDebug`. Build settings are in [app/build.gradle.kts](app/build.gradle.kts).

## Demo data and privacy

Use fictional records only. Passwords are stored locally, and clinician access uses a fixed demo code. The app is an educational prototype for exploring workflows.

Optional Gemini analysis sends the selected patient's display name, medication details and symptom context to the provider. To enable it during development, set `GEMINI_API_KEY` in the ignored `local.properties` file ([example](local.properties.example)). The build embeds the key in the APK. Keep it out of shared builds.
