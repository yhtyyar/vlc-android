package org.videolan.vlc.kaspresso.tests

import androidx.test.espresso.Espresso
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

/**
 * R.id.ml_menu_filter expands an inline androidx.appcompat.widget.SearchView as the toolbar's
 * collapsible action view (org.videolan.vlc.gui.ContentActivity, which MainActivity extends) —
 * it's not a separate Activity/Fragment, and the expanded SearchAutoComplete input has no fixed
 * resource id of its own. This mirrors the same click-then-dismiss interaction the existing
 * Espresso suite already exercises (see org.videolan.vlc.gui.PlaylistFragmentUITest,
 * "Check search shows").
 */
@Epic("VLC Android")
@Feature("Search")
class SearchTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    @Story("Toolbar search")
    @Severity(SeverityLevel.NORMAL)
    fun openingAndDismissingSearchRestoresTheToolbar() = run {
        step("Search is reachable from the video tab") {
            flakySafely { MainScreen { searchButton.isVisible() } }
        }

        step("Open search") {
            MainScreen { searchButton.click() }
            device.screenshots.take("search_expanded")
        }

        step("Dismissing search restores the toolbar") {
            Espresso.pressBack()
            flakySafely { MainScreen { searchButton.isVisible() } }
        }
    }
}
