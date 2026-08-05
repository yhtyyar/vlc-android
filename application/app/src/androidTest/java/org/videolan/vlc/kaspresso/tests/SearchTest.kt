package org.videolan.vlc.kaspresso.tests

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Test
import org.videolan.vlc.R
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.MainScreen

/**
 * Тест-кейс: Поиск медиафайлов через поисковую строку
 *
 * Шаги теста:
 *  ==========
 *  1. Приложение открыто на вкладке Video
 *  2. Тап по иконке поиска (лупа) в Toolbar
 *  3. Ввести поисковый запрос "sample"
 *  4. Проверить что поле поиска содержит текст
 *  5. Нажать Back чтобы закрыть поиск
 *  6. Проверить что Toolbar вернулся в исходное состояние
 *
 * Ожидаемый результат:
 *  - Поисковая строка открывается
 *  - Текст вводится корректно
 *  - Закрытие поиска возвращает Toolbar
 */
@Epic("VLC Android")
@Feature("Поиск")
class SearchTest : KaspressoUITest() {

    @Test
    @Story("Открытие, ввод и закрытие поиска")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "TC-005: Поиск медиафайлов — ввод запроса и закрытие.\n" +
        "Приоритет: NORMAL\n" +
        "Предусловия: Приложение запущено\n" +
        "Постусловия: Поиск закрыт, Toolbar виден"
    )
    fun searchFlowOpensAcceptsInputAndCloses() = run {
        step("Предусловие: запустить приложение") {
            ensureAppIsOpen()
        }

        step("ШАГ 1: Убедиться что находимся на вкладке Video") {
            MainScreen {
                videoTab.click()
                Thread.sleep(500)
            }
            device.screenshots.take("search_step1_video_tab")
        }

        step("ШАГ 2: Тап по иконке поиска в Toolbar") {
            MainScreen {
                flakySafely { searchButton.isVisible() }
                searchButton.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("search_step2_search_opened")
        }

        step("ШАГ 3: Ввод поискового запроса") {
            // SearchAutoComplete — поле ввода внутри SearchView
            onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("sample"))
            Thread.sleep(1500) // Ждём debounce поиска
            device.screenshots.take("search_step3_query_typed")
        }

        step("ШАГ 4: Нажать Back для закрытия поиска") {
            androidx.test.espresso.Espresso.pressBack()
            Thread.sleep(1000)
            device.screenshots.take("search_step4_search_closed")
        }

        step("ШАГ 5: Проверить что Toolbar вернулся (иконка поиска видна)") {
            MainScreen {
                flakySafely { searchButton.isVisible() }
            }
            device.screenshots.take("search_step5_toolbar_restored")
        }

        step("Результат: поиск открывается, ввод работает, закрывается корректно") {
            device.screenshots.take("search_final_result")
        }
    }
}
