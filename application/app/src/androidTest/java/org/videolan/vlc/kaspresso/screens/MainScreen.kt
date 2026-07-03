package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerView
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
    val fab = KView { withId(R.id.fab) }

    // Present on the toolbar of every top-level tab (org.videolan.vlc.gui.browser.MediaBrowserFragment).
    val searchButton = KView { withId(R.id.ml_menu_filter) }

    // org.videolan.vlc.gui.video.VideoGridFragment (res/layout/video_grid.xml) — only meaningful
    // while the video tab is the active fragment.
    val videoGridList = KRecyclerView(builder = { withId(R.id.video_grid) }, itemTypeBuilder = {})
    val videoGridEmptyState = KView { withId(R.id.empty_loading) }

    // res/layout/audio_recyclerview.xml — shared by the Artists/Albums/Songs sub-tabs under
    // the audio tab (org.videolan.vlc.gui.audio.AudioBrowserFragment); only meaningful there.
    val audioList = KRecyclerView(builder = { withId(R.id.audio_list) }, itemTypeBuilder = {})
}
