package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KTextInputLayout
import org.videolan.vlc.R

/**
 * Page Object для экрана сетевого потока (MRLPanelFragment).
 * Layout: res/layout/mrl_panel.xml
 *
 * Поле ввода URL и кнопка Play для потоковой трансляции.
 */
object NetworkStreamScreen : KScreen<NetworkStreamScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    /** Поле ввода URL потока (TextInputLayout) */
    val urlInputLayout = KTextInputLayout { withId(R.id.mrl_edit) }

    /** Кнопка Play */
    val playButton = KView { withId(R.id.play) }

    /** Список ранее открытых потоков */
    val streamsList = KView { withId(R.id.mrl_list) }
}
