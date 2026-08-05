package org.videolan.vlc.kaspresso.tests

import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Test
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.EqualizerScreen
import org.videolan.vlc.kaspresso.screens.MainScreen
import org.videolan.vlc.kaspresso.screens.MoreScreen
import org.videolan.vlc.kaspresso.screens.SettingsScreen

/**
 * Тест-кейс: Навигация по настройкам приложения
 *
 * Шаги теста:
 *  ==========
 *  1. Приложение открыто
 *  2. Перейти на More → Settings
 *  3. Проверить что список настроек отображается
 *  4. Прокрутить список вниз и проверить что элементы прогружаются
 *  5. Вернуться назад
 *
 * Ожидаемый результат:
 *  - Настройки открываются корректно
 *  - Список настроек прокручивается
 *  - Возврат к меню More работает
 */
@Epic("VLC Android")
@Feature("Настройки")
class SettingsTest : KaspressoUITest() {

    @Test
    @Story("Открытие и навигация по настройкам")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "TC-007: Проверка экрана настроек.\n" +
        "Приоритет: NORMAL\n" +
        "Предусловия: Приложение запущено\n" +
        "Постусловия: Возврат на главный экран"
    )
    fun settingsScreenOpensAndIsScrollable() = run {
        step("Предусловие: запустить приложение") {
            ensureAppIsOpen()
        }

        step("ШАГ 1: Перейти на вкладку More") {
            MainScreen {
                moreTab.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("settings_step1_more_tab")
        }

        step("ШАГ 2: Открыть Settings") {
            MoreScreen {
                settingsButton.click()
                Thread.sleep(2000)
            }
            device.screenshots.take("settings_step2_settings_opened")
        }

        step("ШАГ 3: Проверка видимости списка настроек") {
            SettingsScreen {
                preferencesList.isVisible()
                toolbar.isVisible()
            }
            device.screenshots.take("settings_step3_list_visible")
        }

        step("ШАГ 4: Прокрутить список вниз") {
            SettingsScreen {
                preferencesList.scrollToEnd()
                Thread.sleep(1000)
            }
            device.screenshots.take("settings_step4_scrolled_down")
        }

        step("ШАГ 5: Прокрутить список вверх (возврат к началу)") {
            SettingsScreen {
                preferencesList.scrollTo(0)
                Thread.sleep(1000)
            }
            device.screenshots.take("settings_step5_scrolled_up")
        }

        step("ШАГ 6: Нажать Back — вернуться к меню More") {
            androidx.test.espresso.Espresso.pressBack()
            Thread.sleep(1500)
            MoreScreen {
                settingsButton.isVisible()
            }
            device.screenshots.take("settings_step6_back_to_more")
        }

        step("Результат: настройки открываются, прокрутка работает") {
            device.screenshots.take("settings_final_result")
        }
    }
}
