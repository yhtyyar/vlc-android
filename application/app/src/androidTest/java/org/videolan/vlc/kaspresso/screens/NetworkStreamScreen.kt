package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KTextInputLayout
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.network.MRLPanelFragment] (res/layout/mrl_panel.xml),
 * hosted by SecondaryActivity and reached via [MoreScreen.streamsEntry]. R.id.mrl_edit is the
 * TextInputLayout itself; the child EditText has no id of its own, so it's addressed through
 * KTextInputLayout.edit.
 */
object NetworkStreamScreen : KScreen<NetworkStreamScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val urlInputLayout = KTextInputLayout { withId(R.id.mrl_edit) }
    val playButton = KView { withId(R.id.play) }
    val streamsList = KView { withId(R.id.mrl_list) }
}
