package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.browser.FileBrowserFragment] (res/layout/directory_browser.xml),
 * reached from [MainScreen.directoriesTab]. There is no dedicated "up" button in this layout —
 * navigation up a directory is via the breadcrumb ([breadcrumb]) or the system back button.
 */
object FileBrowserScreen : KScreen<FileBrowserScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val fileList = KRecyclerView(builder = { withId(R.id.network_list) }, itemTypeBuilder = {})
    val breadcrumb = KView { withId(R.id.ariane) }
    val filterButton = KView { withId(R.id.ml_menu_filter) }
    val sortButton = KView { withId(R.id.ml_menu_sortby) }
}
