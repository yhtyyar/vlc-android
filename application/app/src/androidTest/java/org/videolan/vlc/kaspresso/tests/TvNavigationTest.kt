package org.videolan.vlc.kaspresso.tests

import android.view.KeyEvent
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.videolan.television.ui.MainTvActivity
import org.videolan.vlc.kaspresso.KaspressoUITest

/**
 * org.videolan.television.ui.MainTvActivity hosts a Leanback BrowseSupportFragment
 * (org.videolan.television.ui.MainTvFragment), which manages its own internal, unexposed focus
 * state — there are no distinct resource ids to assert a specific row/card gained focus (see
 * ANALYSIS.md). These tests therefore verify the real, checkable outcome of D-pad/remote input on
 * this screen: the activity keeps running and doesn't crash. Precise focus-order assertions are a
 * follow-up that would need Leanback-internal API research beyond this pass's scope.
 */
@Epic("VLC Android TV")
@Feature("D-Pad navigation")
class TvNavigationTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainTvActivity::class.java)

    @Test
    @Story("D-Pad navigation")
    @Severity(SeverityLevel.CRITICAL)
    fun dpadNavigationDoesNotCrashTheBrowseScreen() = run {
        step("Send D-Pad down/right presses across the browse rows") {
            repeat(3) { device.uiDevice.pressKeyCode(KeyEvent.KEYCODE_DPAD_DOWN) }
            repeat(3) { device.uiDevice.pressKeyCode(KeyEvent.KEYCODE_DPAD_RIGHT) }
            device.screenshots.take("tv_dpad_navigation")
        }

        step("The activity is still resumed") {
            assertEquals(Lifecycle.State.RESUMED, activityRule.scenario.state)
        }
    }

    @Test
    @Story("Remote control")
    @Severity(SeverityLevel.NORMAL)
    fun mediaPlayPauseKeyDoesNotCrashTheBrowseScreen() = run {
        step("Send a media play/pause key press") {
            device.uiDevice.pressKeyCode(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }

        step("The activity is still resumed") {
            assertEquals(Lifecycle.State.RESUMED, activityRule.scenario.state)
        }
    }
}
