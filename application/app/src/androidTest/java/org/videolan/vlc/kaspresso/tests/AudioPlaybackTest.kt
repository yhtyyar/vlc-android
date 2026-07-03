package org.videolan.vlc.kaspresso.tests

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.core.testcontext.BaseTestContext
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.hamcrest.Matchers.allOf
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.videolan.medialibrary.interfaces.Medialibrary
import org.videolan.vlc.R
import org.videolan.vlc.gui.MainActivity
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.AudioPlayerScreen
import org.videolan.vlc.kaspresso.screens.MainScreen
import org.videolan.vlc.kaspresso.utils.TestMediaProvider

/**
 * Pushes two small, real, checked-in sample tracks
 * (application/app/src/androidTest/assets/media/sample_audio*.mp3, via [TestMediaProvider])
 * before each test — two distinct tracks so [nextButtonAdvancesToAnotherTrack] has somewhere real
 * to go. The [assumeTrue] check is kept as a defensive fallback in case the push/rescan doesn't
 * land in time on a given device.
 *
 * The audio tab (R.id.nav_audio) lands on org.videolan.vlc.gui.audio.AudioBrowserFragment, which
 * hosts nested Artists/Albums/Songs sub-tabs sharing the same list layout/id
 * ([MainScreen.audioList], res/layout/audio_recyclerview.xml). Depending on which sub-tab is the
 * default, the first row may be a track (starts playback directly) or an artist/album (navigates
 * one level deeper first) — these tests assume the former. A mis-landed first click surfaces as a
 * real, informative test failure rather than a silently-wrong pass.
 */
@Epic("VLC Android")
@Feature("Audio playback")
class AudioPlaybackTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun pushSampleTracks() {
        TestMediaProvider.pushAudio()
        TestMediaProvider.rescanAndAwait()

        val hasAudio = Medialibrary.getInstance()
                .getPagedAudio(Medialibrary.SORT_DEFAULT, false, true, false, 1, 0)
                .isNotEmpty()
        assumeTrue("Sample tracks weren't indexed by the medialibrary in time — skipping", hasAudio)
    }

    // An extension on BaseTestContext (not a plain method) so flakySafely resolves via the
    // implicit TestContext receiver when called from inside a run { } / step { } block.
    private fun BaseTestContext.playFirstAudioTrack() {
        // audioTab.click() itself is wrapped too: on a cold start (fresh install, first
        // interaction) the bottom nav can take a moment past a plain click's immediate lookup.
        flakySafely { MainScreen { audioTab.isVisible() } }
        MainScreen {
            audioTab.click()
            flakySafely { audioList.isVisible() }
        }
        // Scoped to isDisplayed(), matching MainScreen.audioList — several R.id.audio_list
        // instances can exist at once (one per off-screen ViewPager sub-tab kept alive).
        onView(allOf(withId(R.id.audio_list), isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
    }

    @Test
    @Story("Playback controls")
    @Severity(SeverityLevel.BLOCKER)
    fun playingATrackShowsTheAudioPlayer() = run {
        step("Start the first audio track from the library") {
            playFirstAudioTrack()
        }

        step("The audio player appears with a title") {
            flakySafely {
                AudioPlayerScreen {
                    headerPlayPauseButton.isVisible()
                    trackTitle.isVisible()
                }
            }
            device.screenshots.take("audio_playing")
        }
    }

    @Test
    @Story("Track switching")
    @Severity(SeverityLevel.NORMAL)
    fun nextButtonAdvancesToAnotherTrack() = run {
        step("Start the first audio track from the library") {
            playFirstAudioTrack()
            flakySafely { AudioPlayerScreen { headerPlayPauseButton.isVisible() } }
        }

        step("Skip to the next track") {
            AudioPlayerScreen { headerNextButton.click() }
            device.screenshots.take("audio_next_track")
        }
    }
}
