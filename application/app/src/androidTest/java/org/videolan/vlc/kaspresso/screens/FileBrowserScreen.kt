package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.browser.FileBrowserFragment] (res/layout/directory_browser.xml).
 *
 * This is *not* what [MainScreen.directoriesTab] shows at its root — that's
 * [MainBrowserScreen] (org.videolan.vlc.gui.browser.MainBrowserFragment), confirmed for real via
 * a CI failure where these ids weren't found there at all. `FileBrowserFragment` is one level
 * deeper, reached by clicking into a specific storage entry from [MainBrowserScreen.localEntry].
 * There is no dedicated "up" button in this layout — navigation up a directory is via the
 * breadcrumb ([breadcrumb]) or the system back button.
 *
 * [fileList] and [emptyState] are mutually exclusive: `BaseBrowserFragment.updateEmptyView()`
 * (confirmed for real) sets `network_list` to GONE and shows `empty_loading` (state EMPTY)
 * whenever the current location has nothing to browse, and vice versa. [breadcrumb] is GONE at
 * this fragment's own root and only appears once a folder has been entered from there.
 */
object FileBrowserScreen : KScreen<FileBrowserScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val fileList = KRecyclerView(builder = { withId(R.id.network_list) }, itemTypeBuilder = {})
    val emptyState = KView { withId(R.id.empty_loading) }
    val breadcrumb = KView { withId(R.id.ariane) }
    val filterButton = KView { withId(R.id.ml_menu_filter) }
    val sortButton = KView { withId(R.id.ml_menu_sortby) }
}
