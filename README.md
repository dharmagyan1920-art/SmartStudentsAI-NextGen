# SmartStudentsAI-NextGen

Android app (Kotlin, Jetpack Compose, Material 3, MVVM) for Indian students, Classes 1-12.

## Status (honest)
Implemented: board / class / medium / stream selection (saved on device), data-driven subjects, chapters,
lessons and learning resources from a bundled **SAMPLE** JSON file, search, empty/loading/error states.

NOT implemented: teacher/admin tools (no login, no backend), AI tutor, AI notes/quizzes, uploads/OCR,
progress tracking, parent dashboard, games, courses, cloud sync. The in-app "What works today" screen lists this too.

All curriculum content in `app/src/main/assets/curriculum/sample_curriculum.json` is sample data written for
development. It is not official, not complete and not verified. The app is not affiliated with any board.

## Build
GitHub Actions (`.github/workflows/android-build.yml`) runs on pushes to `main` and manually. It runs unit tests,
builds the debug APK and uploads it as the `SmartStudentsAI-debug-apk` artifact (plus logs as `build-logs-and-reports`).
The workflow installs Gradle itself, so it works without `gradlew`.
Locally: open in Android Studio (JDK 17), or `gradle testDebugUnitTest assembleDebug`.

## Adding curriculum
Edit the JSON (or add another `CurriculumRepository`). No composable needs to change.
