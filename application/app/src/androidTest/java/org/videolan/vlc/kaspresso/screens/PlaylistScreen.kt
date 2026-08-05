package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.videolan.vlc.R

/**
 * Page Object для экрана плейлистов (PlaylistFragment).
 *
 * Доступен через таб Playlists на главном экране.
 * Содержит список треков с контекстным меню.
 */
object PlaylistScreen : KScreen<PlaylistScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val trackList = KRecyclerView(
        builder = { withId(R.id.audio_list); isDisplayed() },
        itemTypeBuilder = {}
    )

    val emptyState = KTextView { withId(R.id.empty_loading) }
    val contextMenuList = KView { withId(R.id.ctx_list) }
}
