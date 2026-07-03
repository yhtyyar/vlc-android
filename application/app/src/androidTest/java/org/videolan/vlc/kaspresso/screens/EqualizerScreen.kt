package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import org.videolan.vlc.R

/**
 * Two real, distinct screens live under this Page Object:
 * - [org.videolan.vlc.gui.EqualizerSettingsActivity] (res/layout/equalizer_settings_activity.xml)
 *   is a management list of saved presets, reached from the "equalizer" preference row.
 * - [org.videolan.vlc.gui.dialogs.EqualizerFragmentDialog] (res/layout/dialog_equalizer.xml) is
 *   the actual enable switch / presets / band sliders, opened from the settings screen's toolbar
 *   via [showEqualizerMenuItem]. Individual bands are added at runtime with generated view ids
 *   (not fixed resource ids), so they aren't exposed here.
 */
object EqualizerScreen : KScreen<EqualizerScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val equalizersList = KView { withId(R.id.equalizers) }
    val showEqualizerMenuItem = KView { withId(R.id.show_equalizer) }

    val enableSwitch = KView { withId(R.id.equalizer_button) }
    val presetsContainer = KView { withId(R.id.equalizer_presets_container) }
    val preampSlider = KView { withId(R.id.equalizer_preamp) }
}
