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

    // main.xml has both a BottomNavigationView (R.id.navigation, phone width) and a
    // NavigationRailView (R.id.navigation_rail, wide layout) sharing the same
    // @menu/bottom_navigation resource, so both always inflate menu items with these same ids —
    // confirmed for real on a Pixel_7a/API 35 emulator (AmbiguousViewMatcherException on a bare
    // withId). Scoping to the BottomNavigationView disambiguates; it's also why the existing
    // Espresso suite never clicks these tabs by id directly (see PlaylistFragmentUITest, which
    // deep-links via an EXTRA_TARGET intent extra instead).
    val videoTab = KView { withId(R.id.nav_video); isDescendantOfA { withId(R.id.navigation) } }
    val audioTab = KView { withId(R.id.nav_audio); isDescendantOfA { withId(R.id.navigation) } }
    val directoriesTab = KView { withId(R.id.nav_directories); isDescendantOfA { withId(R.id.navigation) } }
    val playlistsTab = KView { withId(R.id.nav_playlists); isDescendantOfA { withId(R.id.navigation) } }
    val moreTab = KView { withId(R.id.nav_more); isDescendantOfA { withId(R.id.navigation) } }

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
