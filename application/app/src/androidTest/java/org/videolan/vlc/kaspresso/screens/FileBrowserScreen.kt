package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import org.videolan.vlc.R

/**
 * Page Object для экрана браузера файлов (FileBrowserFragment).
 * Layout: res/layout/directory_browser.xml
 *
 * Показывает содержимое конкретной папки/директории.
 * Для корневого экрана (MainBrowserFragment) см. [MainBrowserScreen].
 */
object FileBrowserScreen : KScreen<FileBrowserScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val fileList = KRecyclerView(
        builder = { withId(R.id.network_list) },
        itemTypeBuilder = {}
    )

    val emptyState = KView { withId(R.id.empty_loading) }
    val breadcrumb = KView { withId(R.id.ariane) }
    val filterButton = KView { withId(R.id.ml_menu_filter) }
    val sortButton = KView { withId(R.id.ml_menu_sortby) }
}
