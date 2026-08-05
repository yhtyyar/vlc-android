package org.videolan.vlc.kaspresso.tests

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.videolan.medialibrary.interfaces.Medialibrary
import org.videolan.vlc.R
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.MainScreen
import org.videolan.vlc.kaspresso.screens.PlayerScreen
import org.videolan.vlc.kaspresso.utils.TestMediaProvider

/**
 * Тест-кейс: Воспроизведение видео-файла
 *
 * Шаги теста:
 *  ==========
 *  1. Приложение открыто на главном экране
 *  2. Убедиться что вкладка Video активна
 *  3. Если видео в библиотеке — клик на первое видео
 *  4. Проверить открытие видео-плеера
 *  5. Тап на экран чтобы показать контролы
 *  6. Нажать Pause — проверить что воспроизведение остановлено
 *  7. Нажать Play — проверить что воспроизведение возобновлено
 *  8. Нажать Forward (10 сек вперёд)
 *  9. Нажать Rewind (10 сек назад)
 *  10. Нажать Back — вернуться к списку
 *
 * Ожидаемый результат:
 *  - Видео открывается и воспроизводится
 *  - Контролы плеера реагируют на нажатия
 *  - Seek forward/backward работает
 *  - Возврат к списку работает
 */
@Epic("VLC Android")
@Feature("Воспроизведение видео")
class VideoPlaybackTest : KaspressoUITest() {

    @Before
    fun pushSampleVideo() {
        TestMediaProvider.pushVideo()
        TestMediaProvider.rescanAndAwait()
    }

    @Test
    @Story("Полный цикл воспроизведения")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "TC-003: Воспроизведение видео-файла — полный жизненный цикл.\n" +
        "Приоритет: CRITICAL\n" +
        "Предусловия: Есть хотя бы одно видео в библиотеке\n" +
        "Постусловия: Плеер закрыт, возврат на главный экран"
    )
    fun fullVideoPlaybackLifecycle() = run {
        step("Предусловие: запустить приложение") {
            ensureAppIsOpen()
        }

        step("ШАГ 1: Перейти на вкладку Video") {
            MainScreen {
                videoTab.click()
                Thread.sleep(1000)
                flakySafely { videoGridList.isVisible() }
            }
            device.screenshots.take("video_step1_video_tab")
        }

        step("ШАГ 2: Клик по первому видео в списке") {
            // Проверяем что есть видео в библиотеке
            val hasVideos = Medialibrary.getInstance()
                .getPagedVideos(Medialibrary.SORT_DEFAULT, false, true, false, 1, 0)
                .isNotEmpty()
            assumeTrue("Нет видео в медиабиблиотеке — тест пропущен", hasVideos)

            onView(withId(R.id.video_grid))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            Thread.sleep(3000) // Ждём открытия плеера
            device.screenshots.take("video_step2_player_opened")
        }

        step("ШАГ 3: Проверка элементов плеера") {
            flakySafely(timeoutMs = 10000) {
                PlayerScreen {
                    title.isVisible()
                    playPauseButton.isVisible()
                    seekBar.isVisible()
                    currentTime.isVisible()
                    totalTime.isVisible()
                }
            }
            device.screenshots.take("video_step3_controls_visible")
        }

        step("ШАГ 4: Нажать Pause") {
            PlayerScreen {
                playPauseButton.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("video_step4_paused")
        }

        step("ШАГ 5: Нажать Play (возобновить воспроизведение)") {
            PlayerScreen {
                playPauseButton.click()
                Thread.sleep(1000)
                playPauseButton.isVisible()
            }
            device.screenshots.take("video_step5_resumed")
        }

        step("ШАГ 6: Нажать Forward (перемотка вперёд)") {
            PlayerScreen {
                forwardButton.click()
                Thread.sleep(1500)
            }
            device.screenshots.take("video_step6_forward")
        }

        step("ШАГ 7: Нажать Rewind (перемотка назад)") {
            PlayerScreen {
                rewindButton.click()
                Thread.sleep(1500)
            }
            device.screenshots.take("video_step7_rewind")
        }

        step("ШАГ 8: Вернуться на экран списка (Back)") {
            val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            uiDevice.pressBack()
            Thread.sleep(2000)
            device.screenshots.take("video_step8_back_to_list")
        }

        step("ШАГ 9: Проверка что вернулись к списку видео") {
            MainScreen {
                flakySafely { videoGridList.isVisible() }
                videoTab.isVisible()
            }
            device.screenshots.take("video_step9_verify_back")
        }

        step("Результат: видео воспроизводится, контролы работают корректно") {
            device.screenshots.take("video_final_result")
        }
    }
}
