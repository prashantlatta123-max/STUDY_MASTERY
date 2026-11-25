# Quality checklist

- [ ] Sync project with Android Studio / Gradle 8.11.1
- [ ] Run `:app:assembleDebug`
- [ ] Run `:app:testDebugUnitTest`
- [ ] Install on API 29 emulator/device
- [ ] Verify first-launch Room seed is inserted exactly once
- [ ] Verify adding subjects/topics survives force-stop and relaunch
- [ ] Verify theme toggle and system status/navigation bars
- [ ] Verify long syllabus descriptions scroll and remain readable
- [ ] Add Room migration tests before changing database schema
- [ ] Add UI tests for subject/topic creation and completion
- [ ] Implement JSON backup/restore with validation before release
- [ ] Verify official syllabus against latest RPSC notification before exam use
