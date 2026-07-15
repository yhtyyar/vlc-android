package org.videolan.vlc.kaspresso.tests

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.junit.Rule
import org.junit.Test
import org.videolan.vlc.R
import org.videolan.vlc.gui.MainActivity
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.FileBrowserScreen
import org.videolan.vlc.kaspresso.screens.MainScreen

/**
 * Whether the root "Directories" tab shows a populated [FileBrowserScreen.fileList] or the
 * [FileBrowserScreen.emptyState] depends on what storage the device/emulator actually exposes
 * (confirmed for real: `BaseBrowserFragment.updateEmptyView()` toggles between the two, and
 * either is a legitimate outcome at the root) — so these tests assert the screen reaches one of
 * its two defined states rather than assuming a specific one.
 */
@Epic("VLC Android")
@Feature("File browser")
class FileBrowserTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    @Story("Navigation")
    @Severity(SeverityLevel.NORMAL)
    fun openingTheDirectoriesTabReachesAPopulatedOrEmptyState() = run {
        step("Open the Directories tab") {
            MainScreen { directoriesTab.click() }
        }

        step("The file list or the empty state is shown") {
            flakySafely {
                onView(
                        allOf(
                                anyOf(withId(R.id.network_list), withId(R.id.empty_loading)),
                                isDisplayed()
                        )
                ).check(matches(isDisplayed()))
            }
            device.screenshots.take("directories_root_state")
        }

        step("The breadcrumb is not shown at the root") {
            FileBrowserScreen { breadcrumb.isGone() }
        }
    }

    @Test
    @Story("Toolbar")
    @Severity(SeverityLevel.NORMAL)
    fun theFilterAndSortMenuItemsAreReachable() = run {
        step("Open the Directories tab") {
            MainScreen { directoriesTab.click() }
        }

        step("Filter and sort menu items are visible regardless of content") {
            flakySafely {
                FileBrowserScreen {
                    filterButton.isVisible()
                    sortButton.isVisible()
                }
            }
        }
    }
}
