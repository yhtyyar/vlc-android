# Kaspresso UI test suite

Additive to the existing Espresso suite in `org.videolan.vlc` (`BaseUITest` and friends) — this
package doesn't replace or modify it. See `/ANALYSIS.md` at the repo root for the full rundown of
what's covered, what's a known follow-up, and how the real resource IDs used here were verified.

## Layout

- `KaspressoConfig.kt` / `KaspressoUITest.kt` — framework setup and the base test class.
- `screens/` — Page Objects (`MainScreen`, `PlayerScreen`, `AudioPlayerScreen`, `FileBrowserScreen`,
  `SettingsScreen`, `EqualizerScreen`, `MoreScreen`, `NetworkStreamScreen`).
- `matchers/` — custom Hamcrest matchers (`VlcMatchers.kt`).
- `utils/` — `TestMediaProvider.kt` pushes the real sample media in `assets/media/` onto the
  device for the playback tests.
- `tests/` — the actual test classes, written as manual-tester-style scripts (TC-00N) rather than
  atomic unit-style tests: `AppLaunchTest` (TC-001), `NavigationTest` (TC-002),
  `VideoPlaybackTest` (TC-003), `AudioPlaybackTest` (TC-004), `SearchTest` (TC-005),
  `MoreMenuTest` (TC-006), `SettingsTest` (TC-007), `FileBrowserTest` (TC-008),
  `NetworkStreamTest` (TC-009).

## Test status

`PerformanceTest`, `KaspressoMigrationShowcaseTest`, `TvNavigationTest`, `SmokeTest`, and
`EqualizerTest` were removed as noisy/low-value; there is currently no dedicated Equalizer or TV
coverage in this suite. See ANALYSIS.md §9 and §11 for the older, now partially superseded,
per-test verification history from before that cleanup — treat it as historical context, not a
statement of the current suite's state.

## Running locally

This repo doesn't commit a Gradle wrapper (`gradlew` is gitignored) and needs a native submodule
that isn't part of this checkout:

```bash
# 1. Install Gradle 9.3.1 (the version this repo's build requires) — e.g. via sdkman:
sdk install gradle 9.3.1

# 2. Fetch libvlcjni (used at Gradle-configuration time; not needed to build these tests, but the
#    :libvlcjni:libvlc project must exist for the multi-project build to configure at all):
git clone --single-branch --branch libvlcjni-3.x https://code.videolan.org/videolan/libvlcjni.git
git -C libvlcjni reset --hard 08cbcce16a2f2cf9ca64f253cc8688d8b55a97da

# 3. Point Gradle at your SDK:
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 4. Compile the androidTest sources (fast, no device needed):
JAVA_HOME=/path/to/jdk17 gradle :application:app:compileDebugAndroidTestKotlin

# 5. Run against a connected device/emulator, scoped to this package only:
JAVA_HOME=/path/to/jdk17 gradle :application:app:connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.package=org.videolan.vlc.kaspresso
```

Step 4 is what CI runs on every push (`.github/workflows/kaspresso-tests.yml`, `compile` job);
step 5 is the `instrumented-tests` job, on an emulator.

## Known limitations

- **TV focus assertions are shallow.** `TvNavigationTest` exercises `MainTvActivity`'s Leanback
  `BrowseSupportFragment` with D-Pad/remote key events, but only asserts the activity survives —
  Leanback manages its own internal, unexposed focus state, so asserting a specific row/card
  gained focus would need further Leanback-internal API research.
- **Allure results pull path in CI is unverified.** See the inline comment in
  `kaspresso-tests.yml` above the "Pull Allure results off the device" step.
- **Reusing one local emulator across many iterations accumulates artifacts** under
  `/storage/emulated/0/Documents/` (screenshots/recordings/logcat this suite's own Kaspresso
  interceptors write) that make `Medialibrary.forceRescan()` (used by `TestMediaProvider`) slow —
  observed taking minutes after ~7 runs. `rescanAndAwait()` bounds the wait at 30s to avoid a hang,
  but if you're iterating locally, periodically clean that directory. A fresh CI emulator doesn't
  have this problem.
- **No Playlist-creation or Subtitle test.** Research found no independent "create empty playlist"
  flow in this app (creation only happens via an existing track's "Add to playlist" context menu,
  already covered by the existing Espresso `PlaylistFragmentUITest`), and the bundled sample video
  has no subtitle track to test against. See ANALYSIS.md §8 for the full reasoning.
