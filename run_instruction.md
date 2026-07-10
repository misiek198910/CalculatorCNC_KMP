# Run Instructions - Kalkulator CNC

## Overview
This repository contains the source code for the **Kalkulator CNC** application. The project is built using **Kotlin Multiplatform (KMP)**, sharing core business logic between platforms, with UI implemented via **Jetpack Compose / Compose Multiplatform**.

## Prerequisites
To successfully build and run this project, ensure you have the following installed:
* **Android Studio**: Latest stable release (ensure the Kotlin Multiplatform plugin is active).
* **JDK**: Java Development Kit 17 or higher.
* **macOS & Xcode**: (Required *only* if you intend to build and run the iOS target).

## How to Build and Run

### 1. Android Target
1. Launch Android Studio.
2. Select **File > Open...** and navigate to the root directory of this repository.
3. Allow Gradle to finish syncing the project.
4. In the run configuration dropdown (top toolbar), select the **`composeApp`** (or **`androidApp`**) configuration.
5. Select a connected Android device or an Android Virtual Device (AVD).
6. Click the **Run** button (or press `Shift + F10`).

**Via Terminal:**
You can also build and install the debug APK directly via the command line:
```bash
./gradlew :composeApp:installDebug