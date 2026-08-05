package org.videolan.vlc.kaspresso.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import org.videolan.vlc.R

/**
 * Page Object для экрана настроек (PreferencesActivity / PreferencesFragment).
 *
 * Settings — это RecyclerView с элементами Preference в виде строк.
 * Каждый элемент имеет заголовок и подзаголовок.
 * Взаимодействие по ключам предпочтений (см. проектные PreferenceMatchers).
 */
object SettingsScreen : KScreen<SettingsScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    val preferencesList = KRecyclerView(
        builder = { withId(R.id.recycler_view) },
        itemTypeBuilder = {}
    )

    val toolbar = KView { withId(R.id.main_toolbar) }
}
