package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.preferences.PreferencesActivity] /
 * [org.videolan.vlc.gui.preferences.PreferencesFragment]. Row-level assertions (by preference
 * key) are already covered by the existing Espresso suite's `PreferenceMatchers`/`onPreferenceRow`
 * (org.videolan.vlc.PreferenceMatcher.kt, org.videolan.vlc.UtilAdapterMatcher.kt) — reuse those
 * from within a test rather than re-implementing preference-row matching here.
 */
object SettingsScreen : KScreen<SettingsScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val preferencesList = KView { withId(R.id.recycler_view) }
}
