package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.browser.FileBrowserFragment] (res/layout/directory_browser.xml),
 * reached from [MainScreen.directoriesTab]. There is no dedicated "up" button in this layout —
 * navigation up a directory is via the breadcrumb ([breadcrumb]) or the system back button.
 *
 * [fileList] and [emptyState] are mutually exclusive: `BaseBrowserFragment.updateEmptyView()`
 * (confirmed for real) sets `network_list` to GONE and shows `empty_loading` (state EMPTY)
 * whenever the current location has nothing to browse, and vice versa. Which one shows at the
 * root "Directories" tab depends on what storage the device/emulator actually exposes, so tests
 * should assert "one of the two is showing", not assume a specific outcome. [breadcrumb] is GONE
 * at the root and only appears once a folder has been entered.
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
