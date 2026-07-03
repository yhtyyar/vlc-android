package org.videolan.vlc.kaspresso.tests

import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Rule
import org.junit.Test
import org.videolan.vlc.PreferenceMatchers.withKey
import org.videolan.vlc.R
import org.videolan.vlc.gui.preferences.PreferencesActivity
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.EqualizerScreen
import org.videolan.vlc.kaspresso.screens.SettingsScreen
import org.videolan.vlc.onPreferenceRow

/**
 * Launches [PreferencesActivity] directly, matching the existing Espresso suite's convention
 * (see org.videolan.vlc.gui.preferences.PreferencesAudioUITest) rather than navigating there
 * through MainActivity's UI.
 */
@Epic("VLC Android")
@Feature("Equalizer")
class EqualizerTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(PreferencesActivity::class.java)

    private fun openEqualizerRow() {
        SettingsScreen { preferencesList.isVisible() }
        onPreferenceRow(R.id.recycler_view, withKey("equalizer"))!!.perform(click())
    }

    @Test
    @Story("Equalizer settings")
    @Severity(SeverityLevel.NORMAL)
    fun openingEqualizerFromSettingsShowsThePresetList() = run {
        step("Open the equalizer row from the preferences list") {
            openEqualizerRow()
        }

        step("The equalizer preset management screen appears") {
            flakySafely {
                EqualizerScreen { equalizersList.isVisible() }
            }
            device.screenshots.take("equalizer_settings_list")
        }
    }

    @Test
    @Story("Equalizer bands")
    @Severity(SeverityLevel.NORMAL)
    fun openingTheEqualizerDialogShowsTheEnableSwitchAndBands() = run {
        step("Open the equalizer row from the preferences list") {
            openEqualizerRow()
        }

        step("Open the equalizer dialog from the toolbar") {
            flakySafely { EqualizerScreen { showEqualizerMenuItem.isVisible() } }
            EqualizerScreen { showEqualizerMenuItem.click() }
        }

        step("The enable switch, presets and preamp are visible") {
            flakySafely {
                EqualizerScreen {
                    enableSwitch.isVisible()
                    presetsContainer.isVisible()
                    preampSlider.isVisible()
                }
            }
            device.screenshots.take("equalizer_dialog_opened")
        }

        step("Toggling the enable switch works") {
            EqualizerScreen { enableSwitch.click() }
        }
    }
}
