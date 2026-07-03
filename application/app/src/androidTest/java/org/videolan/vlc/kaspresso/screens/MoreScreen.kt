package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.MoreFragment], reached via [MainScreen.moreTab].
 */
object MoreScreen : KScreen<MoreScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    // org.videolan.vlc.gui.view.TitleListView is not itself clickable — confirmed for real (a
    // click on it was a silent no-op). MoreFragment.kt wires navigation to MRLPanelFragment on
    // streamsEntry.setOnActionClickListener, i.e. the compound view's internal R.id.action_button
    // (TitleListView.kt:70-71). historyEntry is a second TitleListView on the same screen with
    // its own action_button, so this must be scoped to streams_entry specifically.
    val streamsEntry = KView { withId(R.id.streams_entry) }
    val streamsActionButton = KView {
        withId(R.id.action_button)
        isDescendantOfA { withId(R.id.streams_entry) }
    }
}
