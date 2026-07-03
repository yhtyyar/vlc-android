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
the fork and is scoped to compiling the new androidTest sources — running
the tests against a real/virtual device is a follow-up, since provisioning a
KVM-accelerated emulator in CI is a separate, more expensive effort than a
first foundational change warrants.
