# Tetris

A classic Tetris game with three implementations — native Kotlin, mobile-first HTML5, and a WebView-packaged APK.

## Implementations

### 1. Native Kotlin (`kotlin/`)

A full Android implementation using custom `View`-based rendering, written in Kotlin.

- Custom `TetrisView` with `Canvas` drawing
- 7-bag randomizer, wall kicks, ghost piece
- Line-clear animation
- Built-in touch buttons with long-press fast-drop
- Gradle project — open in Android Studio or build with `./gradlew assembleRelease`

### 2. HTML5 / PWA (`html/`)

A mobile-first web version that runs directly in the browser. No server required.

- **Adaptive Canvas** — cell size scales to screen width/height and `devicePixelRatio`
- **Touch controls** — swipe on the game area: left/right to move, up to rotate, down to hard-drop, tap to rotate
- **Virtual buttons** — large tap targets at the bottom for thumb-friendly play
- **DAS (Delayed Auto Shift)** — hold left/right buttons for continuous movement
- **Vibration feedback** — haptic pulses on move, rotate, drop, and line clears
- **PWA support** — `manifest.json` + Service Worker for offline play and "Add to Home Screen"
- **Keyboard** — arrow keys / WASD / Space for desktop use
- **Responsive layout** — portrait, landscape, and desktop modes

Open `html/index.html` in any browser to play.

### 3. WebView APK (`android/`)

A thin Android wrapper that loads the HTML5 version in a fullscreen `WebView`.

- Pure Java — no Kotlin, no AppCompat, zero external dependencies
- Immersive fullscreen (hides status bar + navigation bar)
- Portrait-locked
- Min SDK 24 (Android 7.0+) · 17 KB APK

The pre-built APK is at `html/Tetris.apk`.

#### Build from source

```bash
cd android
ANDROID_HOME=/path/to/sdk ./gradlew assembleRelease
# APK output: app/build/outputs/apk/release/app-release.apk
```

Requires Android SDK with platform 34 and build-tools 34+.

## Gameplay

| Action        | Keyboard         | Touch (Canvas)    | Touch (Buttons)     |
|---------------|------------------|-------------------|---------------------|
| Move left     | `←` / `A`        | Swipe left        | Left button         |
| Move right    | `→` / `D`        | Swipe right       | Right button        |
| Rotate        | `↑` / `W`        | Swipe up / Tap    | Rotate button       |
| Soft drop     | `↓` / `S`        | —                 | —                   |
| Hard drop     | `Space`          | Swipe down        | Drop button         |
| Pause         | `P`              | —                 | Pause button        |

**Scoring:** 100 / 300 / 500 / 800 points for 1–4 lines cleared (x level multiplier).  
Soft drop: +1 per row. Hard drop: +2 per row.  
Level increases every 10 lines; drop speed increases with level.

## Project Structure

```
Tetris/
├── kotlin/             # Native Kotlin Android app
│   └── app/src/main/
│       ├── java/com/lzz/tetris/
│       │   ├── MainActivity.kt
│       │   ├── TetrisGame.kt
│       │   └── TetrisView.kt
│       └── res/layout/activity_main.xml
├── html/               # HTML5 / PWA version
│   ├── index.html      # Main game (single-file)
│   ├── manifest.json   # PWA manifest
│   ├── sw.js           # Service Worker
│   └── Tetris.apk      # Pre-built WebView APK
├── android/            # WebView APK project
│   └── app/src/main/
│       ├── java/com/lzz/tetris/MainActivity.java
│       └── assets/index.html
└── .gitignore
```

## License

MIT
