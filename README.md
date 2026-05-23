# iTunes Song Search

Android app to search and preview songs using the public [iTunes Search API](https://developer.apple.com/library/archive/documentation/AudioVideo/Conceptual/iTuneSearchAPI/). Built with Kotlin, Jetpack Compose, and Clean Architecture.

## Features

- Search songs with debounce and Paging 3 (pull-to-refresh, pagination, error states for refresh and append)
- Recently played tracks persisted in Room
- Full-screen **Now Playing** player with ExoPlayer preview clips (30s)
- Album screen with cached songs, pull-to-refresh, and explicit stale/sync error feedback
- Offline search cache, connectivity banner, and edge-to-edge Material 3 dark UI
- Splash screen via AndroidX SplashScreen API

## Demo

![iTunes Song Search demo](docs/demo.gif)

## Tech stack

| Area | Stack |
|------|--------|
| UI | Jetpack Compose, Material 3, Navigation Compose (type-safe routes) |
| Architecture | MVVM, Clean Architecture (domain / data / presentation) |
| DI | Hilt |
| Network | Retrofit, OkHttp |
| Local | Room, Paging 3 `RemoteMediator` |
| Media | Media3 ExoPlayer (in-process, no foreground notification) |
| Images | Coil |
| Tests | JUnit 4, MockK, Turbine, Compose UI Test |

## Requirements

- Android Studio Ladybug or newer
- JDK 11+
- Android SDK 36
- Device or emulator (API 26+)

## Getting started

```bash
git clone https://github.com/arthursz/iTunesSongSearch.git
cd iTunesSongSearch
```

Open the project in Android Studio and run the `app` configuration, or:

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

No API keys are required. The app uses the public iTunes endpoints.

## Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (device/emulator)
./gradlew connectedDebugAndroidTest
```

## Project layout

```
app/src/main/java/com/arthurzettler/itunessongsearch/
├── data/
│   ├── local/         # Room (songs, search cache, now playing)
│   ├── remote/        # Retrofit API, SongRemoteMediator
│   ├── playback/      # ExoPlayerAudioPlayer
│   └── repository/    # SongRepositoryImpl, PlaybackRepositoryImpl
├── domain/
│   ├── model/         # Song, Outcome, NowPlaying, NetworkMonitor
│   └── repository/    # Repository contracts
├── presentation/
│   ├── songs/         # Search + recently played
│   ├── player/        # Now Playing screen
│   ├── album/         # Album detail
│   ├── navigation/    # NavGraph, type-safe routes
│   └── components/    # Shared UI (banner, list items, top bar)
├── di/                # Hilt modules
└── ui/theme/          # Material 3 theme
```

## API

- Search: `GET https://itunes.apple.com/search`
- Album lookup: `GET https://itunes.apple.com/lookup`

Preview audio streams come from iTunes preview URLs (30s clips).

## License

MIT — see [LICENSE](LICENSE).

## Disclaimer

This project is not affiliated with Apple Inc. iTunes is a trademark of Apple Inc.
