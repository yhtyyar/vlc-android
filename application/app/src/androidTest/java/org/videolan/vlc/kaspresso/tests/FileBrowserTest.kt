package org.videolan.vlc.kaspresso.tests

import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Test
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.FileBrowserScreen
import org.videolan.vlc.kaspresso.screens.MainBrowserScreen
import org.videolan.vlc.kaspresso.screens.MainScreen

/**
 * Тест-кейс: Браузер файлов (Browse)
 *
 * Шаги теста:
 *  ==========
 *  1. Приложение открыто
 *  2. Перейти на вкладку Browse
 *  3. Проверить что отображаются секции: Favorites, Local, Network
 *  4. Клик по Local storage
 *  5. Проверить что открылся список файлов/папок
 *  6. Нажать Back
 *  7. Вернуться на Video
 *
 * Ожидаемый результат:
 *  - Корневой экран браузера показывает 3 секции
 *  - Переход в Local storage работает
 *  - Назад возвращает к корню браузера
 */
@Epic("VLC Android")
@Feature("Браузер файлов")
class FileBrowserTest : KaspressoUITest() {

    @Test
    @Story("Навигация по браузеру файлов")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "TC-008: Проверка браузера файлов — корневой экран и переход в Local.\n" +
        "Приоритет: NORMAL\n" +
        "Предусловия: Приложение запущено\n" +
        "Постусловия: Возврат на главный экран"
    )
    fun fileBrowserNavigationWorks() = run {
        step("Предусловие: запустить приложение") {
            ensureAppIsOpen()
        }

        step("ШАГ 1: Перейти на вкладку Browse") {
            MainScreen {
                directoriesTab.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("browser_step1_browse_tab")
        }

        step("ШАГ 2: Проверить что видны все секции корневого экрана") {
            MainBrowserScreen {
                favoritesEntry.isVisible()
                localEntry.isVisible()
                networkEntry.isVisible()
            }
            device.screenshots.take("browser_step2_root_sections")
        }

        step("ШАГ 3: Клик по Local storage") {
            MainBrowserScreen {
                localEntry.click()
                Thread.sleep(2000)
            }
            device.screenshots.take("browser_step3_local_storage_opened")
        }

        step("ШАГ 4: Проверка что открылся список файлов/папок") {
            // Либо список файлов, либо пустое состояние — оба кейса валидны
            FileBrowserScreen {
                // Проверяем что хотя бы один элемент виден (fileList или emptyState)
                flakySafely(timeoutMs = 10000) {
                    try {
                        fileList.isVisible()
                    } catch (e: Exception) {
                        emptyState.isVisible()
                    }
                }
            }
            device.screenshots.take("browser_step4_file_list")
        }

        step("ШАГ 5: Нажать Back — вернуться к корневому экрану браузера") {
            androidx.test.espresso.Espresso.pressBack()
            Thread.sleep(1500)
            MainBrowserScreen {
                localEntry.isVisible()
            }
            device.screenshots.take("browser_step5_back_to_root")
        }

        step("ШАГ 6: Вернуться на вкладку Video") {
            MainScreen {
                videoTab.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("browser_step6_back_to_video")
        }

        step("Результат: браузер файлов работает, навигация корректна") {
            device.screenshots.take("browser_final_result")
        }
    }
}
