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

@Epic("VLC Android")
@Feature("Main navigation")
class SmokeTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    @Story("App launch")
    @Severity(SeverityLevel.BLOCKER)
    fun launchingTheAppShowsTheBottomNavigation() = run {
        step("Bottom navigation tabs are visible") {
            // The medialibrary scan started in KaspressoUITest.startMedialibraryForTest() races
            // with the initial layout, so give the fragment placeholder time to settle.
            flakySafely {
                MainScreen {
                    videoTab.isVisible()
                    audioTab.isVisible()
                    directoriesTab.isVisible()
                    playlistsTab.isVisible()
                }
            }
            device.screenshots.take("main_screen_launched")
        }
    }

    @Test
    @Story("Tab switching")
    @Severity(SeverityLevel.CRITICAL)
    fun switchingToAudioTabSelectsIt() = run {
        step("Switch to the audio tab") {
            MainScreen {
                audioTab.click()
                flakySafely { audioTab.isSelected() }
            }
            device.screenshots.take("audio_tab_selected")
        }

        step("Switch back to the video tab") {
            MainScreen {
                videoTab.click()
                flakySafely { videoTab.isSelected() }
            }
        }
    }

    @Test
    @Story("Tab switching")
    @Severity(SeverityLevel.NORMAL)
    fun switchingToDirectoriesTabUpdatesTheFragmentPlaceholder() = run {
        step("Switch to the directories tab") {
            MainScreen {
                directoriesTab.click()
                flakySafely {
                    directoriesTab.isSelected()
                    fragmentPlaceholder.isVisible()
                }
            }
            device.screenshots.take("directories_tab_selected")
        }
    }
}
