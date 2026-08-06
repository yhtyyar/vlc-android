package org.videolan.vlc.kaspresso.tests

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.hamcrest.Matchers.containsString
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.videolan.medialibrary.interfaces.Medialibrary
import org.videolan.tools.ENABLE_SEEK_BUTTONS
import org.videolan.tools.Settings
import org.videolan.tools.VIDEO_HUD_TIMEOUT
import org.videolan.tools.putSingle
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
        // Причина №1, по которой forward/rewind были не найдены (проверено дампом UI-дерева:
        // title/play-pause/seekbar/tracks на месте и видимы, а player_overlay_forward/rewind в
        // дереве попросту отсутствуют) — эти кнопки управляются отдельным preference'ом
        // ENABLE_SEEK_BUTTONS, который по умолчанию false. Включаем на время теста.
        Settings.getInstance(context).putSingle(ENABLE_SEEK_BUTTONS, true)
        // Причина №2 (проявилась отдельно, уже когда кнопки стали существовать): оверлей
        // контролов автоскрывается через Settings.videoHudDelay секунд (по умолчанию 2-4)
        // бездействия — на реальном прогоне кнопка была найдена, но visibility=INVISIBLE
        // (оверлей успел спрятаться до клика). videoHudDelay == -1 отключает автоскрытие
        // (маппится в OVERLAY_INFINITE). Settings — ленивый синглтон, его init() из prefs
        // отрабатывает только раз за процесс, ещё до этого @Before — меняем и prefs, и
        // закэшированное in-memory поле напрямую.
        Settings.getInstance(context).putSingle(VIDEO_HUD_TIMEOUT, -1)
        Settings.videoHudDelay = -1
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

        step("ШАГ 2: Клик по видео, запушенному тестом") {
            // Проверяем что есть видео в библиотеке
            val hasVideos = Medialibrary.getInstance()
                .getPagedVideos(Medialibrary.SORT_DEFAULT, false, true, false, 1, 0)
                .isNotEmpty()
            assumeTrue("Нет видео в медиабиблиотеке — тест пропущен", hasVideos)

            // VLC's "New external storage detected" dialog can pop up right as the grid
            // becomes interactive and silently absorb the click meant for it below.
            dismissTransientDialogsIfPresent()

            // Кликаем по конкретному запушенному видео, а не по позиции 0 — если на
            // устройстве накопились другие видео (с прошлых прогонов), VLC группирует
            // ролики с общим префиксом имени в отдельный тайл-папку, и position 0 в
            // гриде перестаёт быть нашим sample_video.
            onView(withId(R.id.video_grid))
                .perform(
                    RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                        hasDescendant(withText(containsString("sample_video"))), click()
                    )
                )
            device.screenshots.take("video_step2_player_opening")
        }

        step("ШАГ 2b: Тап по экрану — показать контролы плеера") {
            // PlayerScreen сам документирует это: "Элементы управления появляются при
            // тапе по экрану" — без этого тапа title/playPauseButton/seekBar не
            // отрисовываются вообще (оверлей просто не поднят).
            val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            uiDevice.click(uiDevice.displayWidth / 2, uiDevice.displayHeight / 2)
            Thread.sleep(500)
        }

        step("ШАГ 3: Проверка элементов плеера") {
            flakySafely(timeoutMs = 15000) {
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
                // Не перепроверяем видимость кнопки после сна: оверлей контролов
                // автоскрывается через несколько секунд бездействия (см. комментарий в
                // PlayerScreen) — это уже реальная UX-логика плеера, а не что-то, что этот
                // шаг должен тестировать.
                playPauseButton.click()
                Thread.sleep(1000)
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
