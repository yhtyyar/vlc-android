package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KTextView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.video.VideoPlayerActivity]
 * (res/layout/player_hud.xml + player_hud_right.xml).
 */
object PlayerScreen : KScreen<PlayerScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val playPauseButton = KView { withId(R.id.player_overlay_play) }
    val seekBar = KView { withId(R.id.player_overlay_seekbar) }
    val currentTime = KTextView { withId(R.id.player_overlay_time) }
    val totalTime = KTextView { withId(R.id.player_overlay_length) }
    val title = KTextView { withId(R.id.player_overlay_title) }
}
