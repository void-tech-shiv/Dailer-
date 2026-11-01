# Prism Dialer — OnePlus Nord 3 5G

A native Android dialer project using Kotlin, Jetpack Compose and Android Telecom. Target device: OnePlus Nord 3 5G running Android 16.

## Current implementation
- Premium dark glass keypad with five accent colours.
- Contact browsing with runtime permission.
- Android default Phone app role request.
- Initial Telecom InCallService and incoming/active call UI.
- Answer, decline, mute, speaker and end-call controls.
- Safe keypad call handoff to Android's dialer while the full call experience is tested.

## Build APK
1. Open the **Actions** tab in this repository.
2. Select **Build Prism Dialer APK** and choose **Run workflow**.
3. Download the `Prism-Dialer-debug` artifact from the completed run.
4. Extract the artifact and install `app-debug.apk` on the OnePlus Nord 3.

The APK is a debug build. Test SIM selection, lock-screen calls, Bluetooth, speaker, mute and emergency calling before setting Prism as your everyday default dialer. Keep the stock Phone app available during testing.

## Stack
- Kotlin 2.0.21
- Jetpack Compose + Material 3
- Android Gradle Plugin 8.9.1
- Android SDK 36


## Build fix
Java and Kotlin compilation are both configured for JVM 17 to match the GitHub Actions JDK.
