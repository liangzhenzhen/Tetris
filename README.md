# Tetris

A classic Tetris game for Android with gold-brick visuals and line-clear shatter effects.

## Download

Get the APK: [Tetris.apk](./Tetris.apk)

Or download from [Releases](../../releases).

### Before Installing

| Brand | Action |
|-------|--------|
| Xiaomi MIUI | Settings → Developer options → Turn off **MIUI optimization** |
| Huawei EMUI | Settings → System & updates → Turn off **Pure mode** |
| OPPO ColorOS | Settings → Security → Turn off **External source app check** |
| Others | Settings → Security → Allow **unknown source installs** |

## Controls

| Button | Action |
|--------|--------|
| ← → | Move left / right |
| ↻ | Rotate |
| ↓ tap | Soft drop |
| ↓ hold | Hard drop |
| Pause | Pause / Resume |
| Restart | New game |

## Build from Source

```bash
git clone https://github.com/liangzhenzhen/Tetris.git
cd Tetris

# Configure Android SDK path
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# Build
./gradlew assembleRelease
```

APK will be at `app/build/outputs/apk/release/tetris-release.apk`.

## Tech Stack

- Kotlin + AndroidX
- Custom Canvas rendering
- minSdk 24 / targetSdk 33
- Tested on: Xiaomi Mi 11 Lite (MIUI 13)

## License

MIT
