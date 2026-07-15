package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.browser.MainBrowserFragment] (res/layout/main_browser_fragment.xml),
 * the actual fragment shown at the root of [MainScreen.directoriesTab] — confirmed for real via a
 * CI failure: [FileBrowserScreen] (R.id.network_list / R.id.empty_loading / R.id.ariane, from
 * res/layout/directory_browser.xml) belongs to a *different*, deeper fragment
 * (org.videolan.vlc.gui.browser.FileBrowserFragment) reached only after clicking into one of the
 * three sections below — it is not present at the Directories tab's root.
 *
 * Each section is its own org.videolan.vlc.gui.view.TitleListView (same compound view as
 * [MoreScreen]'s entries) with its own internal RecyclerView + EmptyLoadingStateView; a section
 * being empty (e.g. "No favorite") does not mean the row itself is hidden.
 */
object MainBrowserScreen : KScreen<MainBrowserScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val favoritesEntry = KView { withId(R.id.fav_browser_entry) }
    val localEntry = KView { withId(R.id.local_browser_entry) }
    val networkEntry = KView { withId(R.id.network_browser_entry) }
}
