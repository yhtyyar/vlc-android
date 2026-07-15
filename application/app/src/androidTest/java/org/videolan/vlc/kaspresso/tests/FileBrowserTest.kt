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
import org.videolan.vlc.kaspresso.screens.MainBrowserScreen
import org.videolan.vlc.kaspresso.screens.MainScreen

/**
 * A prior version of this test targeted [org.videolan.vlc.kaspresso.screens.FileBrowserScreen]'s
 * ids (R.id.network_list / R.id.empty_loading), which don't exist at the Directories tab's root —
 * confirmed for real via a CI failure. The root is
 * org.videolan.vlc.gui.browser.MainBrowserFragment ([MainBrowserScreen]); each of its three
 * sections (Favorites/Local storage/Network) is its own TitleListView row, and a row being empty
 * (e.g. "No favorite") doesn't hide the row itself — so asserting these rows are visible is a safe
 * check regardless of what storage the device/emulator actually exposes.
 */
@Epic("VLC Android")
@Feature("File browser")
class FileBrowserTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    @Story("Navigation")
    @Severity(SeverityLevel.NORMAL)
    fun openingTheDirectoriesTabShowsTheBrowseSections() = run {
        step("Open the Directories tab") {
            MainScreen { directoriesTab.click() }
        }

        step("The favorites and local storage sections are shown") {
            flakySafely {
                MainBrowserScreen {
                    favoritesEntry.isVisible()
                    localEntry.isVisible()
                }
            }
            device.screenshots.take("directories_root_sections")
        }
    }
}
