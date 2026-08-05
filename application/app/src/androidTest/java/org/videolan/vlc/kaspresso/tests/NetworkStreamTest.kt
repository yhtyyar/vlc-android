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
import org.videolan.vlc.kaspresso.screens.MoreScreen
import org.videolan.vlc.kaspresso.screens.NetworkStreamScreen

/**
 * Тест-кейс: Сетевые потоки (Network Stream)
 *
 * Шаги теста:
 *  ==========
 *  1. Приложение открыто
 *  2. Перейти на More → Streams
 *  3. Проверить что отображаются поле URL и кнопка Play
 *  4. Ввести тестовый URL
 *  5. Проверить что URL введён
 *  6. Нажать Back
 *
 * Ожидаемый результат:
 *  - Экран Streams открывается
 *  - URL вводится корректно
 *  - Кнопка Play видна и активна
 */
@Epic("VLC Android")
@Feature("Сетевые потоки")
class NetworkStreamTest : KaspressoUITest() {

    @Test
    @Story("Открытие и ввод URL сетевого потока")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "TC-009: Проверка экрана сетевых потоков — ввод URL.\n" +
        "Приоритет: NORMAL\n" +
        "Предусловия: Приложение запущено\n" +
        "Постусловия: Возврат на главный экран"
    )
    fun networkStreamScreenAcceptsUrlInput() = run {
        step("Предусловие: запустить приложение") {
            ensureAppIsOpen()
        }

        step("ШАГ 1: Перейти на вкладку More") {
            MainScreen {
                moreTab.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("stream_step1_more_tab")
        }

        step("ШАГ 2: Открыть Streams") {
            MoreScreen {
                streamsActionButton.click()
                Thread.sleep(2000)
            }
            device.screenshots.take("stream_step2_streams_opened")
        }

        step("ШАГ 3: Проверить элементы экрана сетевого потока") {
            NetworkStreamScreen {
                urlInputLayout.isVisible()
                playButton.isVisible()
            }
            device.screenshots.take("stream_step3_elements_visible")
        }

        step("ШАГ 4: Ввести тестовый URL") {
            NetworkStreamScreen {
                urlInputLayout.edit.typeText("rtsp://test.example/stream")
                Thread.sleep(500)
                urlInputLayout.edit.hasText("rtsp://test.example/stream")
            }
            device.screenshots.take("stream_step4_url_typed")
        }

        step("ШАГ 5: Очистить поле URL") {
            NetworkStreamScreen {
                urlInputLayout.edit.clearText()
                Thread.sleep(500)
            }
            device.screenshots.take("stream_step5_url_cleared")
        }

        step("ШАГ 6: Нажать Back — вернуться к меню More") {
            androidx.test.espresso.Espresso.pressBack()
            Thread.sleep(1500)
            MoreScreen {
                settingsButton.isVisible()
            }
            device.screenshots.take("stream_step6_back_to_more")
        }

        step("ШАГ 7: Вернуться на вкладку Video") {
            MainScreen {
                videoTab.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("stream_step7_back_to_video")
        }

        step("Результат: экран Streams работает, ввод URL корректен") {
            device.screenshots.take("stream_final_result")
        }
    }
}
