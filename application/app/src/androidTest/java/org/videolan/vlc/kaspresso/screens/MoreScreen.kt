package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.videolan.vlc.R

/**
 * Page Object для экрана MoreFragment.
 * Layout: res/layout/more_fragment.xml
 *
 * Экран содержит:
 *  - Кнопки Settings и About вверху
 *  - Секции Streams и History (TitleListView)
 *  - Donations (видна только при определённых условиях)
 */
object MoreScreen : KScreen<MoreScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    // ======== Top Buttons ========
    val settingsButton = KButton { withId(R.id.settingsButton) }
    val aboutButton = KButton { withId(R.id.aboutButton) }

    // ======== Streams Section ========
    val streamsEntry = KView { withId(R.id.streams_entry) }
    val streamsActionButton = KView {
        withId(R.id.action_button)
        isDescendantOfA { withId(R.id.streams_entry) }
    }

    // ======== History Section ========
    val historyEntry = KView { withId(R.id.history_entry) }
    val historyActionButton = KView {
        withId(R.id.action_button)
        isDescendantOfA { withId(R.id.history_entry) }
    }

    // ======== Donations ========
    val donationsButton = KView { withId(R.id.donationsButton) }
}
