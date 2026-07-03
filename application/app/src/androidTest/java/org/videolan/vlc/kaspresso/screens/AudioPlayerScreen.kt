package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KTextView
import org.videolan.vlc.R

/**
 * Page Object for [org.videolan.vlc.gui.audio.AudioPlayer], the single fragment backing both
 * the collapsed mini player bar and the expanded fullscreen player (res/layout/audio_player.xml).
 * There is no separate album field in this layout — only title and artist.
 */
object AudioPlayerScreen : KScreen<AudioPlayerScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    // Expanded player controls
    val playPauseButton = KView { withId(R.id.play_pause) }
    val timeline = KView { withId(R.id.timeline) }
    val trackTitle = KTextView { withId(R.id.title) }
    val artist = KTextView { withId(R.id.artist) }
    val nextButton = KView { withId(R.id.next) }
    val previousButton = KView { withId(R.id.previous) }
    val shuffleButton = KView { withId(R.id.shuffle) }
    val repeatButton = KView { withId(R.id.repeat) }
    val currentTime = KTextView { withId(R.id.time) }
    val totalTime = KTextView { withId(R.id.length) }

    // Collapsed mini player bar controls
    val headerPlayPauseButton = KView { withId(R.id.header_play_pause) }
    val headerNextButton = KView { withId(R.id.header_next) }
    val headerPreviousButton = KView { withId(R.id.header_previous) }
    val progressBar = KView { withId(R.id.progressBar) }
}
