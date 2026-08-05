package org.videolan.vlc.kaspresso.screens

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher
import org.videolan.vlc.R

/**
 * Page Object для главного экрана VLC (MainActivity).
 *
 * Экран содержит:
 *  - BottomNavigationView с 5 табами: Video, Audio, Browse, Playlists, More
 *  - Toolbar с иконкой поиска
 *  - FAB (кнопка "Play all") — видна только на вкладке Video
 *  - Fragment placeholder для контента
 */
object MainScreen : KScreen<MainScreen>() {
    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    // ======== Bottom Navigation Tabs ========
    val videoTab = KView { withMatcher(tabMatcher(R.id.nav_video)) }
    val audioTab = KView { withMatcher(tabMatcher(R.id.nav_audio)) }
    val directoriesTab = KView { withMatcher(tabMatcher(R.id.nav_directories)) }
    val playlistsTab = KView { withMatcher(tabMatcher(R.id.nav_playlists)) }
    val moreTab = KView { withMatcher(tabMatcher(R.id.nav_more)) }

    // ======== Toolbar ========
    val toolbar = KView { withId(R.id.main_toolbar) }
    val searchButton = KView { withId(R.id.ml_menu_filter) }
    val sortButton = KView { withId(R.id.ml_menu_sortby) }

    // ======== Content Area ========
    val fragmentPlaceholder = KView { withId(R.id.fragment_placeholder) }

    // FAB — кнопка "Play all", видна на вкладке Video
    val playAllFab = KView { withId(R.id.fab) }

    // ======== Video Tab Content ========
    // Видео-грид (GridView, не RecyclerView)
    val videoGridList = KView { withId(R.id.video_grid) }
    val videoGridEmptyState = KTextView { withId(R.id.empty_loading) }

    // ======== Audio Tab Content ========
    // RecyclerView со списком аудио-файлов
    val audioList = KRecyclerView(
        builder = { withId(R.id.audio_list); isDisplayed() },
        itemTypeBuilder = {}
    )

    // ======== Playlists Tab Content ========
    val playlistsList = KRecyclerView(
        builder = { withId(R.id.audio_list); isDisplayed() },
        itemTypeBuilder = {}
    )
}

/**
 * Создаёт Hamcrest matcher для bottom-navigation таба.
 * Разрешает неоднозначность между BottomNavigationView и NavigationRailView
 * (один из них скрыт — GONE) за счёт проверки isDisplayed().
 */
private fun tabMatcher(@androidx.annotation.IdRes menuItemId: Int): Matcher<View> {
    return org.hamcrest.Matchers.allOf(
        androidx.test.espresso.matcher.ViewMatchers.withId(menuItemId),
        androidx.test.espresso.matcher.ViewMatchers.isDisplayed(),
        org.hamcrest.Matchers.anyOf(
            androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA(
                org.hamcrest.Matchers.allOf(
                    androidx.test.espresso.matcher.ViewMatchers.withId(R.id.navigation),
                    androidx.test.espresso.matcher.ViewMatchers.isDisplayed()
                )
            ),
            androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA(
                org.hamcrest.Matchers.allOf(
                    androidx.test.espresso.matcher.ViewMatchers.withId(R.id.navigation_rail),
                    androidx.test.espresso.matcher.ViewMatchers.isDisplayed()
                )
            )
        )
    )
}
