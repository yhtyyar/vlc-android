package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.text.KTextView
import org.videolan.vlc.R

/**
 * Page Object для аудио-плеера (AudioPlayer).
 * Layout: res/layout/audio_player.xml
 *
 * Есть два режима отображения:
 *  1. Развёрнутый полноэкранный плеер
 *  2. Сжатый мини-плеер внизу экрана
 */
object AudioPlayerScreen : KScreen<AudioPlayerScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    // ======== Expanded Player Controls ========
    val playPauseButton = KImageView { withId(R.id.play_pause) }
    val nextButton = KImageView { withId(R.id.next) }
    val previousButton = KImageView { withId(R.id.previous) }
    val shuffleButton = KImageView { withId(R.id.shuffle) }
    val repeatButton = KImageView { withId(R.id.repeat) }

    // ======== Track Info ========
    val trackTitle = KTextView { withId(R.id.title) }
    val artist = KTextView { withId(R.id.artist) }
    val coverArt = KImageView { withId(R.id.cover) }

    // ======== Progress ========
    val progressBar = KView { withId(R.id.timeline) }
    val currentTime = KTextView { withId(R.id.time) }
    val totalTime = KTextView { withId(R.id.length) }

    // ======== Mini Player (Collapsed) ========
    val miniPlayPauseButton = KImageView { withId(R.id.header_play_pause) }
    val miniNextButton = KImageView { withId(R.id.header_next) }
    val miniPreviousButton = KImageView { withId(R.id.header_previous) }
    val miniProgressBar = KView { withId(R.id.progressBar) }
    val miniTitle = KTextView { withId(R.id.header_title) }
}
