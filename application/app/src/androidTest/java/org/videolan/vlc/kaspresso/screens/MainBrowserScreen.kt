package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import org.videolan.vlc.R

/**
 * Page Object для корневого экрана браузера (MainBrowserFragment).
 * Layout: res/layout/main_browser_fragment.xml
 *
 * Это первый экран при клике на таб "Browse" (Directories).
 * Содержит три секции: Favorites, Local storage, Network.
 */
object MainBrowserScreen : KScreen<MainBrowserScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val favoritesEntry = KView { withId(R.id.fav_browser_entry) }
    val localEntry = KView { withId(R.id.local_browser_entry) }
    val networkEntry = KView { withId(R.id.network_browser_entry) }
}
