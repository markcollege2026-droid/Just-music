# Just Music — the music-only edition of Flow

This fork (`markcollege2026-droid/Just-music`) is the Flow Android client with its shell trimmed
down to the music surface. Nothing on the music side was redesigned: the same tab page, the same
player sheet, the same shelves, typography, colour and motion are used. What changed is *what the
app offers around them*.

---

## 1. What this codebase is

| | |
|---|---|
| App | Flow 3.0.0 — privacy-first YouTube + YouTube Music client |
| Language / UI | Kotlin 2.4, Jetpack Compose (Material 3 Expressive), Navigation-Compose |
| Playback | Media3 / ExoPlayer, `MediaLibraryService` (background audio), video player on `PlayerView` |
| DI / data | Hilt + KSP, Room (schema 23+), DataStore preferences, Ktor/OkHttp, NewPipeExtractor |
| Build | AGP 9.3.1, Gradle 9.6.1, JDK 17 bytecode / JDK 21 build, compileSdk 37, minSdk 26, targetSdk 36 |
| Flavours | `github` (updater + Discord presence) and `foss` (no updater, no Discord); `debug` / `release` / `nightly` build types, ABI splits + universal APK |

Size of the source tree: **1,834 Kotlin files, ~277k lines** under `app/src/main`
(plus 30+ translated locales and generated Snowball stemmers).

```
app/src/main/java/io/github/aedev/flow
├── MainActivity / TvActivity / FlowApplication      entry points, deep links, PiP
├── data/          (414 files, 67k LOC)  repositories, Room, InnerTube models, downloads, lyrics,
│                                        scrobbling, recognition, local media, recommendation engine
├── innertube/     (141 files, 17k LOC)  InnerTube client: watch/browse/search/next endpoints
├── player/        (162 files, 26k LOC)  video + music playback managers, queues, recovery, audio
├── service/       Media3MusicService (background audio, media session, Android Auto),
│                  VideoPlayerService (popup/PiP)
├── ui/            Compose shell, navigation, screens, components, theme, TV
│   ├── screens/music        (50 files, 8.7k)   music tab, search, artist/album/playlist, browse
│   ├── components/music     (30 files, 4.9k)   music shelves, cards, rows, menus
│   ├── components/musicplayer (49 files, 9.9k) player sheet, lyrics, queue, mini player, EQ sheet
│   ├── screens/library      (22 files, 3.5k)   library + downloads + local media
│   └── tv                   (35 files, 3.9k)  10-foot UI incl. TV music pages
├── widget/        (52 files, 5k)   Glance home-screen widgets (now playing, turntable, on repeat,
│                                   recently played, week, playlist, downloads, recognise, actions)
├── sync/          (41 files, 6.4k) FLOW-SYNC/1 device-to-device sync (QR pairing, merge, crypto)
├── notification/  (12 files)       notification channels, workers, update checks
└── di/            Hilt modules
```

---

## 2. Where the music portion actually lives

```
        ┌──────────────────────────── UI ────────────────────────────┐
        │ EnhancedMusicScreen  (music tab, default start destination) │
        │ MusicSearchScreen · ArtistRoute/ArtistPage · MusicCollection│
        │ YouTubeBrowseScreen · RecognitionScreen · MoodsAndGenres    │
        │ UnifiedMusicPlayerSheet (full / split / compact + lyrics)   │
        │ Mini player · Queue sheet · Audio settings (EQ)             │
        └───────────────▲───────────────────────────┬────────────────┘
                        │ UI state                  │ user intent
        ┌───────────────┴───────────┐   ┌───────────▼────────────────┐
        │ MusicViewModel (1539 ln)  │   │ SharedMusicPlayerViewModel │
        │ MusicSearchViewModel      │   │ MusicPlayerViewModel (636) │
        │ MusicHomeLibraryViewModel │   │ queue / radio / lyrics     │
        └───────────────▲───────────┘   └───────────┬────────────────┘
                        │                           │
        ┌───────────────┴───────────┐   ┌───────────▼────────────────┐
        │ InnertubeMusicService     │   │ EnhancedMusicPlayerManager │
        │ YouTubeMusicService       │   │ Media3MusicService         │
        │ FlowNeuroEngine (local    │   │ (MediaLibraryService,      │
        │ recommender, on-device)   │   │  media session, Auto, EQ)  │
        └───────────────▲───────────┘   └───────────┬────────────────┘
                        │                           │
        ┌───────────────┴───────────┐   ┌───────────▼────────────────┐
        │ data/music, data/newmusic │   │ Media3 ExoPlayer +         │
        │ data/lyrics (7 providers) │   │ audio renderers, cache     │
        │ data/scrobble, Room       │   └────────────────────────────┘
        └───────────────────────────┘
```

**Music features that ship unchanged in this edition:** home feed with locally-ranked shelves
(Quick Picks, Listen again, On repeat, Speed dial, Rediscover, Deep cuts, Daily discover, genres…),
search, artist/album/playlist pages, moods & genres, radio/watch-playlists, queue management,
lyrics (LRCLIB, BetterLyrics, KuGou, SimpMusic, Unison, YouTube captions…), music-video switching,
audio-only mode, equalizer, sleep timer, downloads, offline playback, Shazam-style recognition,
Last.fm / ListenBrainz scrobbling, animated artwork, Android Auto, home-screen widgets, casting
(DLNA), device sync, and the 10-foot TV music pages.

**Video-only (compiled, but no longer presented):** Home feed, Shorts, Subscriptions video feed,
Explore, channel pages, video player screen, Watch later / Liked videos / video playlists / saved
Shorts / notes in the library, video downloads list, video widgets' entry points.

---

## 3. What this edition changes

Everything is driven by one Gradle flag, `MUSIC_ONLY` (`BuildConfig.MUSIC_ONLY`), exposed to Kotlin
as `io.github.aedev.flow.ui.isMusicEdition`. Flip it to `false` in `app/build.gradle.kts` and the
full Flow shell comes back — no file needs re-editing.

| Area | Change | File |
|---|---|---|
| Identity | `applicationId = io.github.aedev.justmusic` (+ `.debug`), version `3.0.0-music` | `app/build.gradle.kts` |
| Flag | `buildConfigField("Boolean", "MUSIC_ONLY", "true")` | `app/build.gradle.kts` |
| Flag accessor | `internal val isMusicEdition` | `ui/MusicEdition.kt` (new) |
| Tabs | Bottom bar / rail show **Music** and **Library** only; Subscriptions, Shorts, Home, Explore and the all-media Search tab are gone | `ui/components/layout/navigation/FlowTab.kt` |
| Start destination | Always opens on the music tab, ignoring any stored default-tab preference | same |
| Library | Shelves reduced to History, Music playlists, Liked music, Downloads (+ Local media, Settings); Watch later, Liked videos, video playlists, saved Shorts and notes removed | `ui/screens/library/LibrarySectionVisibility.kt` |
| Downloads | Opens on the song list instead of the video list | `ui/screens/library/DownloadsScreen.kt` |
| Music home | The "Your subscriptions" lane keeps its artists but loses its see-all arrow, which pointed at the video subscription feed | `ui/components/music/section/MusicHomeLibraryShelves.kt` |
| Onboarding | Channel-picking step skipped (interests → artists → alerts → import → ready), and its summary chip removed | `ui/screens/onboarding/{OnboardingModels,OnboardingViewModel,ReadyStep}.kt` |
| Branding | Launcher name and app name → **Just Music** in every locale | `res/values*/strings.xml` |
| Empty-state home button | The Downloads screen's "Home" button returns to the music tab | `ui/FlowNavigation.kt` |

Deliberately untouched: the music tab layout, shelves, cards, player sheet, lyrics, queue, theme,
motion, widgets, TV layout, and every music screen's internal design.

---

## 4. The APKs

Both files sit next to this repo in the workspace:

| File | Size | Package | Version | Signing |
|---|---|---|---|---|
| `apk/just-music-debug.apk` | 68.4 MB | `io.github.aedev.justmusic.debug` | `3.0.0-music-debug` | debug key, debuggable, no shrinking |
| `apk/just-music-release.apk` | 28.9 MB | `io.github.aedev.justmusic` | `3.0.0-music` | CI build key, R8 + resource shrinking |

SHA-256 (`apk/checksums.txt`):

```
ec30c709ea1e3dac8bee5e37bc205448a7b6ee7771df4dfa79f9bec250587ec7  just-music-debug.apk
122fe437140c19c8f51f258ae339f441fe490c762a43e48dbd9aeb6461cbf281  just-music-release.apk
```

Both are **universal** APKs (arm64-v8a, armeabi-v7a, x86, x86_64), built from commit
`3bc6863` by [run 39488290665](https://github.com/markcollege2026-droid/Just-music/actions/runs/39488290665),
and `apksigner verify --print-certs` passed in that run. The binaries are also published to the
git tag `just-music-apks` (`apk/` inside the tag):

```bash
git fetch origin refs/tags/just-music-apks:refs/tags/just-music-apks
git show just-music-apks:apk/app-foss-universal-release.apk > just-music-release.apk
```

Built by `.github/workflows/just-music-apk.yml` (branch-push/manual workflow that never runs the
upstream release pipeline). It uses the same Gradle/JDK/AGP toolchain as upstream CI:

```bash
./gradlew :app:assembleFossDebug      # app-foss-universal-debug.apk   (debuggable, no shrinking)
./gradlew :app:assembleFossRelease    # app-foss-universal-release.apk (R8 + resource shrinking)
```

* **Flavour `foss`** — no self-updater and no Discord presence, so the build is self-contained and
  never phones home to Flow's release channel.
* **Universal APK** (all four ABIs) so one file installs anywhere; ABI-specific splits are also
  produced by the same Gradle run if you prefer smaller files.
* **Signing** — the debug APK uses the standard debug key. The release APK is signed with a build
  key generated inside the workflow job (`keytool`, alias `justmusic`) because the upstream release
  keystore secret does not exist on a fork; the app id is distinct from `io.github.aedev.flow`, so
  this key only ever has to match itself. Re-sign it with your own keystore if you plan to publish.

The same two files are also attached to every run of the **Just Music APK** workflow as the
`just-music-apks` artifact, and `.github/workflows/just-music-deliver.yml` republishes them to the
`just-music-apks` tag when the artifact store itself is out of reach.

### Installing

```bash
adb install -r apk/just-music-debug.apk      # io.github.aedev.justmusic.debug
adb install -r apk/just-music-release.apk    # io.github.aedev.justmusic
```

Both install side by side with the real Flow client (`io.github.aedev.flow`).

---

## 5. Known edges

* The music player can still show a song's music video ("video" toggle in the player) — that is a
  music feature, so it stays; the standalone video player screen is what the shell no longer leads to.
* Notifications, widgets and Android Auto are shared components; they still list music only when the
  music library is all the app has.
* `FLOW_SYNC` and backup work unchanged; a backup taken in the full app imports into this edition.
* The full-shell unit tests (`FlowTabTest`, `LibrarySectionVisibilityTest`) are now edition-aware:
  they skip under this flag and two music-edition tests assert the trimmed shell instead.
* The launcher icon is still Flow's; only the name changed.
