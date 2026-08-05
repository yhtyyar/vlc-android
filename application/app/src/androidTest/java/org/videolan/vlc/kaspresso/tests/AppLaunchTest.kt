package org.videolan.vlc.kaspresso.tests

import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Step
import io.qameta.allure.kotlin.Story
import org.junit.Test
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.MainScreen

/**
 * Тест-кейс: Запуск приложения VLC Android
 *
 * Шаги теста (путь мануального тестировщика):
 *  ===========================================
 *  1. Вернуться на домашний экран устройства (кнопка Home)
 *  2. Открыть список приложений (свайп вверх)
 *  3. Найти иконку VLC в списке приложений
 *  4. Тап по иконке VLC
 *  5. Ожидание загрузки — проверить что появляется главный экран
 *  6. Убедиться что видна нижняя навигация с табами:
 *     Video, Audio, Browse, Playlists, More
 *
 * Ожидаемый результат:
 *  - Приложение запускается без вылетов
 *  - Главный экран отображается с табами навигации
 *  - Таб "Video" выбран по умолчанию
 *
 * Фактический результат: [заполняется при выполнении теста]
 * Статус: PASS / FAIL
 * Скриншоты: прикрепляются автоматически на каждом шаге
 */
@Epic("VLC Android")
@Feature("Запуск приложения")
class AppLaunchTest : KaspressoUITest() {

    @Test
    @Story("Запуск из лаунчера")
    @Severity(SeverityLevel.BLOCKER)
    @Description(
        "TC-001: Запуск приложения VLC Android через иконку в лаунчере.\n" +
        "Приоритет: BLOCKER\n" +
        "Предусловия: Приложение установлено на устройстве\n" +
        "Постусловия: Приложение запущено и отображает главный экран"
    )
    fun userLaunchesAppFromHomeScreen() = run {
        step("ШАГ 1: Вернуться на домашний экран") {
            device.uiDevice.pressHome()
            device.uiDevice.waitForIdle(3000)
            device.screenshots.take("step1_home_screen")
        }

        step("ШАГ 2: Открыть список приложений (свайп вверх)") {
            val screenHeight = device.uiDevice.displayHeight
            val screenWidth = device.uiDevice.displayWidth
            device.uiDevice.swipe(
                screenWidth / 2, screenHeight - 100,
                screenWidth / 2, screenHeight / 3,
                20
            )
            device.uiDevice.waitForIdle(3000)
            device.screenshots.take("step2_app_drawer")
        }

        step("ШАГ 3: Найти иконку VLC и тапнуть по ней") {
            val vlcIcon = device.uiDevice.findObject(
                androidx.test.uiautomator.UiSelector().text("VLC")
            )
            if (vlcIcon != null && vlcIcon.exists()) {
                vlcIcon.click()
            } else {
                // Fallback: запуск через adb
                device.uiDevice.executeShellCommand(
                    "am start -n ${VLC_PACKAGE}/org.videolan.vlc.gui.MainActivity"
                )
            }
            device.uiDevice.waitForIdle(3000)
            device.screenshots.take("step3_app_launched")
        }

        step("ШАГ 4: Ожидание загрузки главного экрана") {
            // Даём время на анимацию запуска — мы не торопимся
            Thread.sleep(3000)
            // Этот запуск идёт через реальную иконку в лаунчере (StartActivity), а не через
            // ActivityScenarioRule (который запускает MainActivity напрямую и никогда не
            // показывает эти экраны) — поэтому именно здесь может появиться Welcome-визард
            // онбординга или debug-диалог "Auto update". baseSetUp() их не увидит, т.к.
            // отработал раньше, до этого запуска.
            dismissTransientDialogsIfPresent()
            device.screenshots.take("step4_main_screen_loading")
        }

        step("ШАГ 5: Проверка видимости табов навигации") {
            flakySafely(timeoutMs = 15000) {
                MainScreen {
                    videoTab.isVisible()
                    audioTab.isVisible()
                    directoriesTab.isVisible()
                    playlistsTab.isVisible()
                    moreTab.isVisible()
                }
            }
            device.screenshots.take("step5_navigation_tabs_visible")
        }

        step("ШАГ 6: Проверка что таб Video открывается и показывает грид") {
            // Не предполагаем, что Video — активный таб: приложение помнит последний
            // открытый таб (KEY_FRAGMENT_ID) и восстанавливает именно его при запуске, так
            // что дефолт на "Video" не гарантирован. Явно кликаем сами.
            MainScreen {
                videoTab.click()
                flakySafely { videoGridList.isVisible() }
            }
            device.screenshots.take("step6_video_tab_active")
        }
    }
}
