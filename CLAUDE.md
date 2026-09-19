# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

ARMONIA (`it.armonia1403.tempio`) is a native Android shell around an offline
soundboard PWA used to play music cues during Masonic lodge meetings and
ceremonies ("Tempio"). It's a single-Activity app: a full-screen `WebView`
loads a bundled, self-contained `index.html` from local assets. There is no
backend and no network dependency at runtime.

## Build

There is no committed `gradlew`/`gradlew.bat` wrapper — only
`gradle/wrapper/gradle-wrapper.properties` is tracked (Gradle 8.9, AGP 8.7.3,
Kotlin 2.0.21). Open the project root in Android Studio and let it perform
the initial Gradle sync (this generates the wrapper scripts and downloads the
SDK/Gradle as needed; requires internet only for that step). After that:

```
gradlew.bat assembleRelease   # Windows
./gradlew assembleRelease     # Mac/Linux
```

APK output: `app/build/outputs/apk/release/` (or `debug/`). Release builds are
signed with the debug key (`signingConfig = signingConfigs.getByName("debug")`
in `app/build.gradle.kts`) so they install directly on a device without a
personal keystore — fine for personal use, but a real keystore is needed for
Play Store distribution.

There are no tests, lint config, or CI in this repo.

`compileSdk`/`targetSdk` = 35, `minSdk` = 24, Java/Kotlin target 17.

## Architecture

- `app/src/main/java/it/armonia1403/tempio/MainActivity.kt` — the entire
  native layer (85 lines). It creates a single `WebView`, serves the bundled
  app through `WebViewAssetLoader` at
  `https://appassets.androidplatform.net/assets/www/index.html` (a real
  HTTPS origin is required for IndexedDB, which the web app uses to remember
  per-cue track overrides), keeps the screen on
  (`FLAG_KEEP_SCREEN_ON`), hides system bars on focus for an immersive
  fullscreen kiosk-style UI, and forwards the Android back button to
  `WebView.goBack()`. JS is enabled but `allowFileAccess`/`allowContentAccess`
  are disabled — all content must come through the asset loader, not `file://`.
- `app/src/main/assets/www/index.html` — the entire web application (UI,
  audio playback logic, IndexedDB persistence). **This file is a generated
  bundle**: it's a self-extracting wrapper (`__bundler_*` loader in the
  `<head>`) that unpacks base64-embedded assets at load time; it is not
  meant to be hand-edited. Per `LEGGIMI-ANDROID.txt`, the human-editable
  source is a separate file, `Armonia App.dc.html`, which is **not present in
  this repo** — `index.html` is regenerated from it. If asked to change app
  behavior/UI and the `.dc.html` source isn't available, be aware edits to
  `index.html` are edits to a build artifact.
- `app/src/main/assets/www/audio/` — 40 MP3s. `01.mp3`…`12.mp3` are a legacy
  set from the original single-ceremony build and are **no longer referenced
  anywhere in the current `index.html`** (dead weight in the APK; don't
  assume changing one of these affects playback). The other ~29 files are
  named classical/Masonic tracks (e.g. `bach-air.mp3`,
  `mozart-zauberflote-sicurezza.mp3`) that make up the in-app `LIBRARY`
  catalog the current cue system actually plays from.
- MP3s are excluded from resource compression
  (`androidResources { noCompress += listOf("mp3") }` in
  `app/build.gradle.kts`) so playback starts without decompression delay.

## Domain behavior (soundboard semantics)

The web app models several distinct **riti** (rites), not one fixed board:
three routine lodge meetings ("Tornata" for 1°/2°/3° grado) and three special
ceremonies ("Iniziazione", "Aumento di salario", "Elevazione"). Each rite has
its own ordered list of cues grouped into named phases (e.g. for the default
1° grado tornata: "Sala dei Passi Perduti" → "Apertura dei Lavori" →
"Chiusura dei Lavori" → "Ove richiesto" → "Pause nel Tempio").

For the default rite (1° grado tornata, 12 cues):
- Cue 1 = Sala dei Passi Perduti, cue 12 = Pause nel Tempio.
- Cues 5 and 9 (opening/closing of the Bible) are intentionally the same
  audio track.
- Cue 11 = national anthem.

Playback mechanics (shared across all rites): tap a cue = play; second tap =
fade out; "SOTTO VOCE" mode ducks the volume; STOP cuts playback immediately.
Looping is a per-cue user toggle (persisted with the other prefs), not a
fixed rule tied to specific cue numbers.

Each rite has its own default track assignment (`BUNDLED` for the 1° grado
tornata, similarly named maps for the others) pulled from the shared
`LIBRARY` of bundled tracks. From the TRACCE screen, users can additionally
override any cue with a track picked from the phone; a manual override takes
precedence over both the bundled default and the library, and is persisted
via IndexedDB (why the app must be served from a real origin rather than
`file://`).

Track-to-cue defaults, phase labels, and per-rite cue lists live in the
`RITI`/`BUNDLED`/`LIBRARY` constants inside the `Armonia App.dc.html` source
(not this repo) — grep the generated `index.html` for these names if you
need to trace exact current assignments.

## Language note

Source comments, the README (`LEGGIMI-ANDROID.txt`), and UI strings are in
Italian; match that when editing user-facing text or comments in this area.
