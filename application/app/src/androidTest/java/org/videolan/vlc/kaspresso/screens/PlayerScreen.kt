package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.progress.KSeekBar
import io.github.kakaocup.kakao.text.KTextView
import org.videolan.vlc.R

/**
 * Page Object для видео-плеера (VideoPlayerActivity).
 * Экран контролов: res/layout/player_hud.xml + player_hud_right.xml
 *
 * Элементы управления появляются при тапе по экрану.
 * После нескольких секунд бездействия контролы исчезают автоматически.
 */
object PlayerScreen : KScreen<PlayerScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    // ======== Playback Controls ========
    val playPauseButton = KImageView { withId(R.id.player_overlay_play) }
    val rewindButton = KImageView { withId(R.id.player_overlay_rewind) }
    val forwardButton = KImageView { withId(R.id.player_overlay_forward) }

    // ======== Seek & Time ========
    val seekBar = KSeekBar { withId(R.id.player_overlay_seekbar) }
    val currentTime = KTextView { withId(R.id.player_overlay_time) }
    val totalTime = KTextView { withId(R.id.player_overlay_length) }

    // ======== Title & Info ========
    val title = KTextView { withId(R.id.player_overlay_title) }
    val subtitleTrackButton = KImageView { withId(R.id.player_overlay_tracks) }

    // ======== Options ========
    val orientationLockButton = KImageView { withId(R.id.orientation_toggle) }

    // ======== Loading ========
    val loadingSpinner = KView { withId(R.id.player_overlay_loading) }
}
