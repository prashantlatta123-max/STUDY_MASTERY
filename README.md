# STUDY MASTERY by SAROTECH

An offline-first Android study planner and syllabus tracker. Initial release includes a Compose dashboard, subject/topic management, Room persistence, and preloaded RPSC School Lecturer Paper-I and Chemistry Paper-II syllabus outline.

## Requirements
- Android Studio with JDK 17
- Android Gradle Plugin 8.9.2 / Gradle 8.11.1
- Android SDK 35
- Minimum Android 10 (API 29)

## Build
Open the project in Android Studio and sync Gradle, or use the included GitHub Actions workflow. The CI workflow installs Gradle 8.11.1 and runs `gradle :app:assembleDebug :app:testDebugUnitTest`.

## Included in this starter build
- Multi-subject database model (not Chemistry-hardcoded)
- Preloaded RPSC Paper-I subject outline
- Preloaded RPSC Chemistry Paper-II syllabus: 10 Senior Secondary, 10 Graduation, 9 Post-Graduation units and 2 Pedagogy/ICT sections
- Add custom subjects and topics
- Mark topics complete / not started
- Offline Room database
- Light/dark theme toggle

## Important scope note
This is the first development milestone, not the finished feature set. Flashcards, spaced-repetition scheduling, MCQ authoring/import, tests, analytics, teaching-practice logs, multilingual UI, backup/restore, and polished adaptive blur are planned follow-up modules. The syllabus content is a structured outline derived from the linked RPSC PDF; check the latest notification before exam use.

## Official source
RPSC Chemistry Paper-II: https://rpsc.rajasthan.gov.in/Static/Syllabus/C3C3C454-7825-4F2A-9989-74D25582DA7D.pdf
RPSC Paper-I: https://rpsc.rajasthan.gov.in/Static/Syllabus/B37784DE-1435-4A01-B500-1A089E9CC9F8.pdf
