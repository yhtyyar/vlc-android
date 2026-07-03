package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.MainActivity] (res/layout/main.xml).
 */
object MainScreen : KScreen<MainScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val videoTab = KView { withId(R.id.nav_video) }
    val audioTab = KView { withId(R.id.nav_audio) }
    val directoriesTab = KView { withId(R.id.nav_directories) }
    val playlistsTab = KView { withId(R.id.nav_playlists) }
    val moreTab = KView { withId(R.id.nav_more) }

    val fragmentPlaceholder = KView { withId(R.id.fragment_placeholder) }
}
