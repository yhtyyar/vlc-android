# VLC-Android UI test landscape: analysis for a Kaspresso foundation

This document summarizes the real state of the repository at the time this
analysis was written (branch `test/kaspresso-framework`), so the added
Kaspresso-based test code can be judged against what actually exists rather
than assumptions.

## 1. Existing UI test suite

The app module already has a mature Espresso-based UI test suite under
`application/app/src/androidTest/java/org/videolan/vlc/`:

- `BaseUITest.kt` — abstract base used by every Espresso UI test. It grants
  `READ_EXTERNAL_STORAGE`, exposes `context` via `ApplicationProvider`, and
  starts the medialibrary in `@Before` with `TestCoroutineContextProvider`
  before calling an abstract `beforeTest()` hook.
- `UtilViewMatchers.kt` / `UtilViewActions.kt` / `UtilAdapterMatcher.kt` /
  `PreferenceMatcher.kt` — custom Hamcrest matchers and Espresso actions
  (`withRecyclerView`, `MediaRecyclerViewMatcher`, `TabsMatcher`,
  `sizeOfAtLeast`, `withCount`, `orientationLandscape`/`orientationPortrait`,
  preference-row matchers), already covering most of what a new framework
  would otherwise have to reinvent.
- `gui/PlaylistFragmentUITest.kt`, `gui/HeaderMediaListActivityUITest.kt`,
  `gui/browser/*UITest.kt`, `gui/preferences/*UITest.kt` — real, working
  tests launching `MainActivity` via `ActivityTestRule` + an `Intent` with
  `EXTRA_TARGET` to land on a specific tab, then asserting with plain
  Espresso (`onView`/`withId`/`matches`).
- `TvScreenhotsInstrumentedTest.kt` / `PhoneScreenhotsInstrumentedTest.kt` —
  screenshot tests using `tools.fastlane:screengrab`.
- `MultidexTestRunner.kt` — the configured `testInstrumentationRunner`; it
  installs MultiDex before delegating to `AndroidJUnitRunner`. This is
  required for instrumentation tests to run at all on devices/emulators
  where the app's method count needs multidex.

There is **no `CONTRIBUTING.md`, `.editorconfig`, ktlint, or detekt config**
anywhere in the repository. Code style was inferred directly from the
existing test files instead: no KDoc on test classes/methods, English inline
comments used sparingly, `whenX_checkY()` / `checkXSetting()` test naming,
8-space continuation indent, `Thread.sleep` used directly for async waits
(no idling resources).

Commit style (from `git log`): short, imperative, frequently
`Area: description` (e.g. `Equalizer: fix initial focus`,
`gradle: enable resValues feature`), no ticket references, generally no
trailing period.

## 2. Kaspresso and Kakao: real API surface

The task originally assumed Kaspresso APIs that don't exist. Since a wrong
import produces a hard compile failure and not a review comment, the real
API surface was confirmed by downloading `kaspresso-1.6.1.aar` and
`kakao-3.6.5.aar` from Maven Central and inspecting the bytecode directly
(`javap` / `strings` on the extracted `.class` files) rather than trusting
documentation from memory:

- Kaspresso's Page Object base is `com.kaspersky.kaspresso.screens.KScreen`
  (abstract `layoutId: Int?`, `viewClass: Class<*>?`), which extends
  `io.github.kakaocup.kakao.screen.Screen`. There is **no**
  `com.kaspersky.kaspresso.viewhiers` package.
- `KView`, `KTextView`, `KButton`, `KRecyclerView`, `KImageView` live in
  `io.github.kakaocup.kakao.*` (Kakao moved from `com.agoda.kakao` to
  `io.github.kakaocup` as an org; `com.agoda.kakao:kakao` on Maven Central
  stops at 2.4.0 and is not what modern Kaspresso depends on).
- `TestCase` (`com.kaspersky.kaspresso.testcases.api.testcase.TestCase`)
  takes a `Kaspresso.Builder` in its constructor, not a built `Kaspresso`
  instance. `Kaspresso.Builder.simple { ... }` is the real, documented way
  to get a builder with sane defaults and customize it.
- `flakySafely` is a member function of `BaseTestContext`
  (`com.kaspersky.kaspresso.testcases.core.testcontext`), available inside a
  `step { ... }` block — not a top-level import.
- `ScreenshotMakeInterceptor`, `ToleranceInterceptor`, and
  `AutoAllowPermissionBehaviorInterceptor` do not exist under those names.
  The real screenshot-on-failure hooks are
  `ScreenshotStepWatcherInterceptor` / `ScreenshotFailStepWatcherInterceptor`
  under `com.kaspersky.kaspresso.interceptors.watcher.testcase.impl.screenshot`.

## 3. Real UI components used by this foundation

### 3.1 Main screen

- Activity: `org.videolan.vlc.gui.MainActivity`
  (`application/vlc-android/src/org/videolan/vlc/gui/MainActivity.kt`)
- Layout: `application/vlc-android/res/layout/main.xml`
- Confirmed real ids:
  - `R.id.navigation` — `BottomNavigationView` (phone layout)
  - `R.id.navigation_rail` — `NavigationRailView` (wide layout), same
    `@menu/bottom_navigation` menu resource as the bottom nav
  - `R.id.nav_video`, `R.id.nav_audio`, `R.id.nav_directories`,
    `R.id.nav_playlists`, `R.id.nav_more` — menu item ids shared by both nav
    variants
  - `R.id.fragment_placeholder` — `FragmentContainerView` hosting the
    current tab's fragment
  - `R.id.fab` — the floating action button
- Existing tests navigate to a specific tab on launch via
  `Intent().putExtra(EXTRA_TARGET, R.id.nav_playlists)` passed to
  `ActivityTestRule.launchActivity(intent)` (see `PlaylistFragmentUITest`).

### 3.2 Video player

- Activity: `org.videolan.vlc.gui.video.VideoPlayerActivity`
  (`application/vlc-android/src/org/videolan/vlc/gui/video/VideoPlayerActivity.kt`)
- Layouts: `application/vlc-android/res/layout/player_hud.xml` and
  `player_hud_right.xml`
- Confirmed real ids:
  - `R.id.player_overlay_play` — `ImageView`,
    `contentDescription="@string/play"`, wired to `player.doPlayPause()`
  - `R.id.player_overlay_seekbar` — `org.videolan.vlc.gui.view.AccessibleSeekBar`
  - `R.id.player_overlay_time` / `R.id.player_overlay_length` — current /
    total time `TextView`s
  - `R.id.player_overlay_title` — title `TextView` (in `player_hud_right.xml`)

## 4. What this pass covers vs. what's a follow-up

This pass adds a **verified, compiling foundation**, additive to the
existing Espresso suite (different package, different base-class name, same
`MultidexTestRunner`):

- `org.videolan.vlc.kaspresso.KaspressoConfig` — a real `Kaspresso.Builder`
- `org.videolan.vlc.kaspresso.KaspressoUITest` — base test case
- `org.videolan.vlc.kaspresso.screens.MainScreen` — Page Object for the
  bottom navigation
- `org.videolan.vlc.kaspresso.screens.PlayerScreen` — Page Object for the
  player HUD
- `org.videolan.vlc.kaspresso.tests.SmokeTest` — real, launchable smoke
  tests around navigation

**Explicitly out of scope for this pass** (would require a running
emulator/device with real media and, for some, a TV form factor, to verify
correctly rather than guess at behavior):

- Audio player Page Object and playback tests
- File browser / preferences Page Objects (the existing Espresso suite
  already covers these — see `gui/browser/*UITest.kt`,
  `gui/preferences/*UITest.kt`)
- Android TV D-pad navigation tests
- Custom matchers for the equalizer, subtitle overlay, or Picture-in-Picture
- Full Allure HTML report generation and publishing in CI (this pass wires
  the Allure annotations/dependencies but does not stand up report
  generation infrastructure)

## 4.1 Verified compilation

`:application:app:compileDebugAndroidTestKotlin` was run locally against Gradle
9.3.1 / JDK 17 (this repo's `gradlew` is intentionally gitignored; contributors
install Gradle 9.3.1 themselves, and `libvlcjni` is fetched separately by
`buildsystem/compile.sh` — both were reproduced locally purely to validate
this change). It builds clean alongside the existing 30+ Espresso tests. The
only new warning is that `Kaspresso.Builder.Companion.withAllureSupport()` is
marked deprecated upstream ("doesn't support storage system restrictions");
there is no verified non-deprecated replacement yet, so it's left as a
follow-up rather than swapped for an unverified API.

## 5. CI

`origin` for this repository is the contributor's own GitHub fork
(`yhtyyar/vlc-android`); `upstream` (`videolan/vlc-android`) uses GitLab CI
and has no `.github/` directory. A GitHub Actions workflow is meaningful on
the fork. It now has two jobs: `compile` (unchanged from the first pass) and
`instrumented-tests`, which runs `org.videolan.vlc.kaspresso.*` on a
KVM-accelerated emulator via `reactivecircus/android-emulator-runner` and
uploads Allure results as an artifact. The on-device Allure results path used
in the "pull" step is the documented convention, not something verified
end-to-end (no emulator was available while authoring it) — see the inline
comment in `kaspresso-tests.yml`.

## 6. Second pass: expanded coverage

Added `screens/AudioPlayerScreen.kt`, `screens/FileBrowserScreen.kt`,
`screens/SettingsScreen.kt`, `matchers/VlcMatchers.kt` (`isProgressChanging`,
polled via `flakySafely`), and `tests/VideoPlaybackTest.kt` /
`tests/AudioPlaybackTest.kt` / `tests/TvNavigationTest.kt`. Two more real
constraints surfaced and are handled explicitly rather than papered over:

- **No media test fixtures.** Confirmed there is no adb-push script,
  androidTest `assets/` folder, or synthetic-`MediaWrapper`-insertion helper
  anywhere in this repo that provisions real playable media (the only
  synthetic `MediaWrapper` usage, in `PhoneScreenhotsInstrumentedTest`, is
  never inserted into the real Medialibrary and isn't playable). Existing
  Espresso tests that touch the medialibrary (e.g. `PlaylistFragmentUITest`)
  depend on whatever the device/emulator already has indexed. `VideoPlaybackTest`
  and `AudioPlaybackTest` check for existing media in `@Before` and skip via
  `org.junit.Assume.assumeTrue` on an empty library rather than fail.
- **`MainTvActivity` wasn't on the androidTest classpath.** Confirmed
  `application/app/build.gradle` had `implementation`/`testImplementation
  project(':application:television')` but no `androidTestImplementation` —
  added it. `TvNavigationTest` sends D-Pad/remote key events at
  `org.videolan.television.ui.MainTvActivity`'s Leanback `BrowseSupportFragment`
  and asserts the activity survives (`Lifecycle.State.RESUMED`); Leanback
  manages its own internal, unexposed focus state, so a more precise
  "row N gained focus" assertion is a follow-up needing Leanback-internal API
  research beyond this pass.

Real IDs added, confirmed by reading the actual layouts (not invented):
`audio_player.xml` (play_pause/header_play_pause/header_large_play_pause,
timeline, title/artist — no album field exists in this layout),
`directory_browser.xml` (network_list, ariane breadcrumb — no dedicated "up"
button), `video_grid.xml` (video_grid, empty_loading),
`audio_recyclerview.xml` (audio_list, shared across the audio tab's
Artists/Albums/Songs sub-tabs), and `player_hud.xml`'s
`player_overlay_rewind`/`player_overlay_forward` jump buttons. Settings
row-level matching reuses the existing `PreferenceMatchers`/`onPreferenceRow`
helpers (`org.videolan.vlc.PreferenceMatcher.kt`,
`org.videolan.vlc.UtilAdapterMatcher.kt`) rather than reimplementing them.

`compileDebugAndroidTestKotlin` was re-run after these additions and is
clean (two real bugs were caught and fixed in the process: `flakySafely`
isn't reachable from a plain private method, only from inside a `run`/`step`
block or a `BaseTestContext` extension function; and `ActivityScenario` has
no nested `State` type — the real type is `androidx.lifecycle.Lifecycle.State`).

## 7. Third pass: actually running the suite on a real emulator

A report of a CI failure prompted this pass, but no real CI run or log could
be found to confirm it (no `gh` CLI available, no linked run). Rather than
"fix" an unconfirmed failure from guesses, this suite was run for real: found
Android Studio's bundled AVDs already on this machine (`~/.android/avd/`,
several phone images plus two Android TV images), booted `Pixel_7a` (API 35,
x86_64) under KVM, built `assembleDebug` + `assembleDebugAndroidTest`, and
ran `connectedDebugAndroidTest` scoped to `org.videolan.vlc.kaspresso`
end-to-end, iterating against the real logcat/screenshots/exceptions each
time — not the speculative causes a generic "there were failing tests" error
suggests (medialibrary init order, `Assume` misbehaving, runner conflicts).
It did fail the first time, for four real, unrelated reasons, all now fixed:

1. **Missing modern runtime permissions.** `KaspressoUITest` only granted
   `READ_EXTERNAL_STORAGE`, a no-op for media access on API 33+.
   `org.videolan.vlc.util.Permissions.canReadStorage()` checks
   `READ_MEDIA_VIDEO`/`READ_MEDIA_AUDIO` instead, and without
   `POST_NOTIFICATIONS` granted up front `MainActivity.onCreate()`'s
   `NotificationPermissionManager.launchIfNeeded()` call has to ask for it at
   runtime. Fixed by granting all four in `GrantPermissionRule`.
2. **A debug-only dialog stealing window focus.** On every fresh install
   (which the test task always does), `MainActivity.onCreate()`'s
   `lifecycleScope.launch` block shows a "nightly build update" `AlertDialog`
   whenever `settings.contains(KEY_SHOW_UPDATE)` is false — confirmed via
   `WindowLeaked` logcat entries pointing at that exact line every run. This
   is a real gap in `KaspressoUITest`, not a preexisting app bug: the
   existing Espresso suite's `BaseUITest` has the exact same gap (it also
   only grants `READ_EXTERNAL_STORAGE` and never seeds `KEY_SHOW_UPDATE`),
   which likely just went unnoticed because that suite predates these
   Android-13+/debug-build behaviors or was last verified on an older
   emulator image — worth flagging upstream rather than assuming it's fine.
   Fixed with a base-class `@Rule` (ordered to wrap outside any subclass's
   `ActivityScenarioRule`, since JUnit rules from the superclass wrap
   outermost) that pre-seeds `KEY_SHOW_UPDATE` before the activity launches.
3. **Ambiguous nav ids.** `main.xml` has both a `BottomNavigationView`
   (`R.id.navigation`) and a `NavigationRailView` (`R.id.navigation_rail`)
   inflating the *same* `@menu/bottom_navigation` resource, so a bare
   `withId(R.id.nav_video)` matches two views
   (`AmbiguousViewMatcherException`). This is exactly why the existing
   Espresso suite never clicks these tabs by id and instead deep-links via
   an `EXTRA_TARGET` intent extra. Fixed by scoping `MainScreen`'s tab
   matchers with `isDescendantOfA { withId(R.id.navigation) }`.
4. **Self-inflicted medialibrary contamination.** `Kaspresso.Builder.withAllureSupport()`
   records a screen video per test to
   `/storage/emulated/0/Documents/video/<Class>/<test>/Video_<Class>.mp4` by
   default — and VLC's own medialibrary scanner indexes those `.mp4` files
   as playable videos, so `VideoPlaybackTest`'s "is there a video" check saw
   its own prior run's recordings and proceeded to click one, which isn't
   real playable content. Fixed by filtering out paths under `/Documents/`
   in the `Assume` check; the device was also manually cleaned for this
   verification run.

Final result on `Pixel_7a`/API 35: 10 tests, 5 passed (`SmokeTest`,
`TvNavigationTest`), 5 correctly skipped via `Assume` (`VideoPlaybackTest`,
`AudioPlaybackTest` — this stock emulator image has no real video/audio
content, confirming §4's "no media fixtures" limitation is accurate), 0
failed. The real on-device Allure artifact root is confirmed to be
`/storage/emulated/0/Documents/` (screenshots/video/logcat/view_hierarchy
subfolders) — but the specific `allure-results` path the CI workflow's
"pull" step guesses at was not found there or in app-private storage before
the test task uninstalled the app, so that step remains genuinely unverified
(disclosed in the workflow's inline comment).
