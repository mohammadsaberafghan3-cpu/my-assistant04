# ORION Android — initial native core

This is an early native Android foundation, separate from the existing browser/PWA prototype.

## Included
- Native Kotlin + Jetpack Compose application shell.
- Persian RTL interface.
- Local conversation history, editable local memory and task text using Android SharedPreferences.
- Deterministic offline commands: help, date, time, note and task.
- No network permission, analytics, account, or API key handling.

## Not included yet
- General-purpose language model or bundled offline LLM.
- Autonomous multi-step agent or Android app control.
- Notifications, background service, voice activation, cloud sync, encrypted vault.
- Production signing. The workflow creates a debug APK for installation/testing only.

## Build
GitHub Actions workflow: `.github/workflows/android-apk.yml`.
Run it manually from Actions or push changes under `android/`. Download the `ORION-debug-apk` artifact from the completed workflow run.
