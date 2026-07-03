package org.videolan.vlc.kaspresso.tests

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.core.testcontext.BaseTestContext
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.videolan.medialibrary.interfaces.Medialibrary
import org.videolan.vlc.R
import org.videolan.vlc.gui.MainActivity
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.matchers.isProgressChanging
import org.videolan.vlc.kaspresso.screens.MainScreen
import org.videolan.vlc.kaspresso.screens.PlayerScreen

/**
 * Requires the medialibrary to have already indexed at least one real video file. This repo has
 * no mechanism to provision that on a fresh emulator (see ANALYSIS.md, "Media test fixtures"), so
 * these tests skip themselves via [assumeTrue] rather than fail on an empty library.
 */
@Epic("VLC Android")
@Feature("Video playback")
class VideoPlaybackTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun requireAtLeastOneVideo() {
        val hasVideos = Medialibrary.getInstance()
                .getPagedVideos(Medialibrary.SORT_DEFAULT, false, true, false, 1, 0)
                .isNotEmpty()
        assumeTrue("No video indexed by the medialibrary on this device/emulator — skipping", hasVideos)
    }

    // An extension on BaseTestContext (not a plain method) so flakySafely resolves via the
    // implicit TestContext receiver when called from inside a run { } / step { } block.
    private fun BaseTestContext.playFirstVideo() {
        MainScreen {
            videoTab.click()
            flakySafely { videoGridList.isVisible() }
        }
        onView(withId(R.id.video_grid))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
    }

    @Test
    @Story("Playback controls")
    @Severity(SeverityLevel.BLOCKER)
    fun playingAVideoShowsThePlayerControls() = run {
        step("Start the first video from the library") {
            playFirstVideo()
        }

        step("Player controls appear") {
            flakySafely {
                PlayerScreen {
                    playPauseButton.isVisible()
                    seekBar.isVisible()
                }
            }
            device.screenshots.take("video_playing")
        }
    }

    @Test
    @Story("Playback controls")
    @Severity(SeverityLevel.CRITICAL)
    fun pausingAndResumingKeepsThePlayerControlsVisible() = run {
        step("Start the first video from the library") {
            playFirstVideo()
            flakySafely { PlayerScreen { playPauseButton.isVisible() } }
        }

        step("Pause") {
            PlayerScreen { playPauseButton.click() }
            device.screenshots.take("video_paused")
        }

        step("Resume") {
            PlayerScreen {
                playPauseButton.click()
                playPauseButton.isVisible()
            }
        }
    }

    @Test
    @Story("Seeking")
    @Severity(SeverityLevel.NORMAL)
    fun tappingForwardMovesTheSeekBarProgress() = run {
        step("Start the first video from the library") {
            playFirstVideo()
            flakySafely { PlayerScreen { forwardButton.isVisible() } }
        }

        step("Jump forward and check the seek bar progress changed") {
            // isProgressChanging() captures the progress on its first call (and reports no match
            // yet) then reports a match once a later call observes a different value — so the
            // matcher instance must survive across flakySafely's retries. The first retry after
            // clicking forward primes it against the post-seek value; a later retry catches
            // ongoing playback moving it further, confirming the player is still advancing.
            val progressChanged = isProgressChanging()
            PlayerScreen { forwardButton.click() }

            flakySafely {
                onView(withId(R.id.player_overlay_seekbar)).check(matches(progressChanged))
            }
        }
    }
}
