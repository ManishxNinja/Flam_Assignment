# 🧠 Edge Detection R&D Intern Project

## 📱 Android + OpenCV + OpenGL + Web

### 🚀 Features
- Camera2 live feed
- C++ OpenCV edge detection (JNI)
- OpenGL ES rendering for smooth output
- TypeScript web viewer for sample frames

### ⚙️ Setup
1. Install Android SDK + NDK + CMake
2. Download OpenCV SDK → place in `/OpenCV/`
3. Build via `./gradlew assembleDebug`
4. Install on device: `adb install app/build/outputs/apk/debug/app-debug.apk`

### 🧩 Architecture
- `/app` → Android + JNI
- `/jni` → C++ native OpenCV
- `/gl` → OpenGL renderer
- `/web` → TypeScript web viewer

