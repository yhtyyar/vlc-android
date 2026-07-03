package org.videolan.vlc.kaspresso

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.videolan.resources.util.startMedialibrary
import org.videolan.vlc.util.TestCoroutineContextProvider

/**
 * Base class for the Kaspresso UI test suite (org.videolan.vlc.kaspresso.*), additive to the
 * existing Espresso suite based on [org.videolan.vlc.BaseUITest]. Mirrors its medialibrary
 * startup so screens backed by the media list aren't stuck loading.
 */
@RunWith(AndroidJUnit4::class)
abstract class KaspressoUITest : TestCase(KaspressoConfig.builder) {

    @Rule
    @JvmField
    val storagePermissionGrant: GrantPermissionRule = GrantPermissionRule.grant(
            "android.permission.READ_EXTERNAL_STORAGE")

    val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun startMedialibraryForTest() {
        context.startMedialibrary(coroutineContextProvider = TestCoroutineContextProvider())
    }
}
