package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.recycler.KRecyclerView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.PlaylistFragment], reached from [MainScreen.playlistsTab].
 * Only exposes what [org.videolan.vlc.kaspresso.tests.KaspressoMigrationShowcaseTest] needs — the
 * item's own context menu (R.id.ctx_list, R.id.item_more) is addressed via the existing project
 * helper `org.videolan.vlc.withRecyclerView` directly in that test, matching how the original
 * Espresso test (`org.videolan.vlc.gui.PlaylistFragmentUITest`) already does it. Kaspresso doesn't
 * require replacing every existing Espresso helper — it wraps the flow around them.
 */
object PlaylistScreen : KScreen<PlaylistScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val audioList = KRecyclerView(builder = { withId(R.id.audio_list) }, itemTypeBuilder = {})
}
