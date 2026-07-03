package org.videolan.vlc.kaspresso.tests

import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Rule
import org.junit.Test
import org.videolan.vlc.gui.MainActivity
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.MainScreen
import org.videolan.vlc.kaspresso.screens.MoreScreen
import org.videolan.vlc.kaspresso.screens.NetworkStreamScreen

/**
 * There's no dedicated menu entry for this on MainActivity's toolbar — the real entry point is
 * the "Streams" row in the More tab (org.videolan.vlc.gui.MoreFragment), which opens
 * SecondaryActivity showing MRLPanelFragment. This doesn't actually start playback of a real
 * network resource (no network fixture available), so it only verifies the dialog's controls are
 * reachable and interactive.
 */
@Epic("VLC Android")
@Feature("Network Streaming")
class NetworkStreamTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    @Story("Open network stream")
    @Severity(SeverityLevel.NORMAL)
    fun openingStreamsFromTheMoreTabShowsTheUrlInput() = run {
        step("Open the More tab") {
            MainScreen {
                moreTab.click()
            }
            flakySafely { MoreScreen { streamsEntry.isVisible() } }
        }

        step("Open Streams") {
            MoreScreen { streamsActionButton.click() }
        }

        step("The URL input and play button appear") {
            flakySafely {
                NetworkStreamScreen {
                    urlInputLayout.isVisible()
                    playButton.isVisible()
                }
            }
            device.screenshots.take("network_stream_opened")
        }

        step("Typing a URL is possible") {
            NetworkStreamScreen {
                urlInputLayout.edit.typeText("rtsp://example.invalid/stream")
                urlInputLayout.edit.hasText("rtsp://example.invalid/stream")
            }
        }
    }
}
