package org.videolan.vlc.kaspresso

import android.content.Context
import android.os.Build
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

    // GrantPermissionRule tolerates a permission that exists on the current API level but isn't
    // dangerous/required — it does NOT tolerate one the OS doesn't know about at all. Confirmed
    // for real in CI (API 30 emulator): granting READ_MEDIA_VIDEO there throws
    // IllegalArgumentException: Unknown permission, since it (and READ_MEDIA_AUDIO,
    // POST_NOTIFICATIONS) was only introduced in API 33. So these are only requested on API 33+,
    // where org.videolan.vlc.util.Permissions.canReadStorage() actually checks them instead of
    // READ_EXTERNAL_STORAGE — confirmed for real on a Pixel_7a/API 35 emulator: without them
    // granted there, VideoPlaybackTest saw an empty medialibrary and skipped via Assume even with
    // real video files present.
    @Rule
    @JvmField
    val storagePermissionGrant: GrantPermissionRule = GrantPermissionRule.grant(
            *buildList {
                add("android.permission.READ_EXTERNAL_STORAGE")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add("android.permission.READ_MEDIA_VIDEO")
                    add("android.permission.READ_MEDIA_AUDIO")
                    add("android.permission.POST_NOTIFICATIONS")
                }
            }.toTypedArray())

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
