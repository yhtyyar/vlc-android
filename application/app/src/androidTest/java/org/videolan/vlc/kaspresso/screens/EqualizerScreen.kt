package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import org.videolan.vlc.R

/**
 * Page Object для экрана эквалайзера.
 * Содержит два экрана:
 *  1. EqualizerSettingsActivity — список пресетов
 *  2. EqualizerFragmentDialog — диалог с enable switch, пресетами и ползунками
 */
object EqualizerScreen : KScreen<EqualizerScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    // ======== Presets List Screen ========
    val equalizersList = KView { withId(R.id.equalizers) }
    val toolbarMenuButton = KImageView { withId(R.id.show_equalizer) }

    // ======== Equalizer Dialog ========
    val enableSwitch = KView { withId(R.id.equalizer_button) }
    val presetsContainer = KView { withId(R.id.equalizer_presets_container) }
    val preampSlider = KView { withId(R.id.equalizer_preamp) }
}
