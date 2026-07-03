package org.videolan.vlc.kaspresso

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.videolan.resources.util.startMedialibrary
import org.videolan.tools.KEY_SHOW_UPDATE
import org.videolan.tools.Settings
import org.videolan.tools.putSingle
import org.videolan.vlc.util.TestCoroutineContextProvider

/**
 * Base class for the Kaspresso UI test suite (org.videolan.vlc.kaspresso.*), additive to the
 * existing Espresso suite based on [org.videolan.vlc.BaseUITest]. Mirrors its medialibrary
 * startup so screens backed by the media list aren't stuck loading.
 */
@RunWith(AndroidJUnit4::class)
abstract class KaspressoUITest : TestCase(KaspressoConfig.builder) {

    // GrantPermissionRule no-ops any permission not applicable to the current SDK level, so it's
    // safe to request the modern (API 33+) media/notification permissions alongside the legacy
    // one. org.videolan.vlc.util.Permissions.canReadStorage() checks READ_MEDIA_VIDEO/AUDIO on
    // API 33+, not READ_EXTERNAL_STORAGE — confirmed for real: without it granted, VideoPlaybackTest
    // saw an empty medialibrary and skipped via Assume even on a device that does have video files.
    @Rule
    @JvmField
    val storagePermissionGrant: GrantPermissionRule = GrantPermissionRule.grant(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.POST_NOTIFICATIONS")

    val context: Context = ApplicationProvider.getApplicationContext()

    // Declared in the base class so it wraps outside any @Rule ActivityScenarioRule declared in
    // a subclass (JUnit orders rules discovered via reflection with superclass fields outermost),
    // running before the activity launches. Confirmed for real on a Pixel_7a/API 35 emulator: on
    // a fresh install (which the test task always installs), MainActivity.onCreate()'s
    // lifecycleScope.launch block shows a debug-only "nightly build update" AlertDialog whenever
    // settings.contains(KEY_SHOW_UPDATE) is false — this steals window focus, so Espresso reports
    // "No views in hierarchy found" for the bottom nav even though MainActivity's own content view
    // (built earlier in onCreate, unconditionally) does have it.
    @Rule
    @JvmField
    val skipNightlyUpdateDialog = object : ExternalResource() {
        override fun before() {
            Settings.getInstance(context).putSingle(KEY_SHOW_UPDATE, true)
        }
    }

    @Before
    fun startMedialibraryForTest() {
        context.startMedialibrary(coroutineContextProvider = TestCoroutineContextProvider())
    }
}
