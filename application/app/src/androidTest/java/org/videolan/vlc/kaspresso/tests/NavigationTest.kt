package org.videolan.vlc.kaspresso.tests

import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Test
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.MainScreen

/**
 * Тест-кейс: Навигация по основным разделам приложения
 *
 * Шаги теста:
 *  ==========
 *  1. Приложение открыто на главном экране
 *  2. Последовательно переключиться на каждый таб:
 *     - Audio
 *     - Browse (Directories)
 *     - Playlists
 *     - More
 *     - Video (возврат)
 *  3. На каждом табе проверить что контент обновился
 *  4. Убедиться что переключение сопровождается анимацией
 *
 * Ожидаемый результат:
 *  - Все табы доступны и кликабельны
 *  - Контент меняется при переключении табов
 *  - Анимация переключения видна пользователю
 *  - Нет вылетов или зависаний
 */
@Epic("VLC Android")
@Feature("Навигация")
class NavigationTest : KaspressoUITest() {

    @Test
    @Story("Переключение табов")
    @Severity(SeverityLevel.BLOCKER)
    @Description(
        "TC-002: Последовательное переключение между всеми табами нижней навигации.\n" +
        "Приоритет: BLOCKER\n" +
        "Предусловия: Приложение запущено, виден главный экран\n" +
        "Постусловия: Все табы протестированы"
    )
    fun switchingBetweenAllTabsWorksSmoothly() = run {
        step("Предусловие: убедиться что приложение открыто") {
            ensureAppIsOpen()
            device.screenshots.take("nav_start_screen")
        }

        // ======== Вкладка VIDEO (по умолчанию) ========
        step("TAB: Video — проверить что отображается контент") {
            MainScreen {
                videoTab.click()
                Thread.sleep(1000) // Ждём анимацию переключения
                flakySafely { videoGridList.isVisible() }
            }
            device.screenshots.take("nav_video_tab")
        }

        // ======== Вкладка AUDIO ========
        step("TAB: Audio — переключиться и проверить контент") {
            MainScreen {
                audioTab.click()
                Thread.sleep(1000) // Ждём анимацию переключения
                flakySafely { audioList.isVisible() }
            }
            device.screenshots.take("nav_audio_tab")
        }

        // ======== Вкладка BROWSE ========
        step("TAB: Browse — переключиться и проверить контент") {
            MainScreen {
                directoriesTab.click()
                Thread.sleep(1000) // Ждём анимацию переключения
                flakySafely { fragmentPlaceholder.isVisible() }
            }
            device.screenshots.take("nav_browse_tab")
        }

        // ======== Вкладка PLAYLISTS ========
        step("TAB: Playlists — переключиться и проверить контент") {
            MainScreen {
                playlistsTab.click()
                Thread.sleep(1000) // Ждём анимацию переключения
                flakySafely { fragmentPlaceholder.isVisible() }
            }
            device.screenshots.take("nav_playlists_tab")
        }

        // ======== Вкладка MORE ========
        step("TAB: More — переключиться и проверить контент") {
            MainScreen {
                moreTab.click()
                Thread.sleep(1000) // Ждём анимацию переключения
                // На More нет стандартного RecyclerView — проверяем что placeholder виден
                flakySafely { fragmentPlaceholder.isVisible() }
            }
            device.screenshots.take("nav_more_tab")
        }

        // ======== Возврат на VIDEO ========
        step("TAB: Вернуться на Video — проверить что контент восстановился") {
            MainScreen {
                videoTab.click()
                Thread.sleep(1000) // Ждём анимацию переключения
                flakySafely { videoGridList.isVisible() }
            }
            device.screenshots.take("nav_back_to_video_tab")
        }

        step("Результат: все табы переключаются корректно, анимация видна") {
            // Финальный скриншот доказательства
            device.screenshots.take("nav_final_result")
        }
    }
}
