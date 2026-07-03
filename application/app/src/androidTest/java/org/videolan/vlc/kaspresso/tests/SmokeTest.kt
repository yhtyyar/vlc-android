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
            MainScreen {
                videoTab.isVisible()
                audioTab.isVisible()
                directoriesTab.isVisible()
                playlistsTab.isVisible()
            }
        }
    }

    @Test
    @Story("Tab switching")
    @Severity(SeverityLevel.CRITICAL)
    fun switchingToAudioTabSelectsIt() = run {
        step("Switch to the audio tab") {
            MainScreen {
                audioTab.click()
                audioTab.isSelected()
            }
        }

        step("Switch back to the video tab") {
            MainScreen {
                videoTab.click()
                videoTab.isSelected()
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
                directoriesTab.isSelected()
                fragmentPlaceholder.isVisible()
            }
        }
    }
}
