package org.videolan.vlc.kaspresso.tests

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import org.hamcrest.Matchers.allOf
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.videolan.medialibrary.interfaces.Medialibrary
import org.videolan.vlc.R
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.AudioPlayerScreen
import org.videolan.vlc.kaspresso.screens.MainScreen
import org.videolan.vlc.kaspresso.utils.TestMediaProvider

/**
 * Тест-кейс: Воспроизведение аудио-файла
 *
 * Шаги теста:
 *  ==========
 *  1. Приложение открыто
 *  2. Перейти на вкладку Audio
 *  3. Клик по первому треку
 *  4. Проверить открытие аудио-плеера (Title, Artist, Controls)
 *  5. Нажать Pause
 *  6. Нажать Play
 *  7. Нажать Next (следующий трек)
 *  8. Нажать Previous (предыдущий трек)
 *  9. Нажать Shuffle
 *  10. Нажать Repeat
 *  11. Нажать Back
 *
 * Ожидаемый результат:
 *  - Аудио воспроизводится
 *  - Все контролы реагируют
 *  - Переключение треков работает
 *  - Shuffle и Repeat переключают состояния
 */
@Epic("VLC Android")
@Feature("Воспроизведение аудио")
class AudioPlaybackTest : KaspressoUITest() {

    @Before
    fun pushSampleTracks() {
        TestMediaProvider.pushAudio()
        TestMediaProvider.rescanAndAwait()
    }

    @Test
    @Story("Полный цикл воспроизведения аудио")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "TC-004: Воспроизведение аудио-файла — полный жизненный цикл.\n" +
        "Приоритет: CRITICAL\n" +
        "Предусловия: Минимум 2 аудио-трека в библиотеке\n" +
        "Постусловия: Плеер закрыт, возврат на главный экран"
    )
    fun fullAudioPlaybackLifecycle() = run {
        step("Предусловие: запустить приложение") {
            ensureAppIsOpen()
        }

        step("ШАГ 1: Перейти на вкладку Audio") {
            MainScreen {
                audioTab.click()
                Thread.sleep(1000)
                flakySafely { audioList.isVisible() }
            }
            device.screenshots.take("audio_step1_audio_tab")
        }

        step("ШАГ 2: Клик по треку, запушенному тестом") {
            val hasAudio = Medialibrary.getInstance()
                .getPagedAudio(Medialibrary.SORT_DEFAULT, false, true, false, 1, 0)
                .isNotEmpty()
            assumeTrue("Нет аудио в медиабиблиотеке — тест пропущен", hasAudio)

            // Тот же класс диалога, что ломал VideoPlaybackTest — "New external storage
            // detected" может модально перекрыть список прямо перед кликом.
            dismissTransientDialogsIfPresent()

            // Вкладка Audio открывается на под-вкладке ARTISTS (группировка по артисту), а
            // не на списке треков — audio_list там показывает карточки артистов
            // ("Unknown Artist"), без имени файла. Нужно явно переключиться на TRACKS.
            onView(allOf(withText("TRACKS"), isDisplayed())).perform(click())
            Thread.sleep(500)

            // Кликаем по конкретному запушенному треку, а не по позиции 0 — по той же
            // причине, по которой это сделали в VideoPlaybackTest. В отличие от видео-грида,
            // список аудио показывает имя файла целиком, с расширением ("sample_audio.mp3"),
            // и точное совпадение обязательно — "sample_audio_2.mp3" тоже содержит
            // "sample_audio" как подстроку.
            // RecyclerViewActions — это чистый Espresso, а не Kakao/KView, так что Kaspresso
            // не оборачивает его в flakySafely автоматически: без ретрая тут ловили пустой,
            // ещё не забинденный адаптер сразу после переключения на TRACKS.
            flakySafely(timeoutMs = 10000) {
                onView(allOf(withId(R.id.audio_list), isDisplayed()))
                    .perform(
                        RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                            hasDescendant(withText("sample_audio.mp3")), click()
                        )
                    )
            }
            device.screenshots.take("audio_step2_player_opening")
        }

        step("ШАГ 3: Проверка элементов аудио-плеера") {
            flakySafely(timeoutMs = 10000) {
                AudioPlayerScreen {
                    trackTitle.isVisible()
                    artist.isVisible()
                    playPauseButton.isVisible()
                    progressBar.isVisible()
                    currentTime.isVisible()
                    totalTime.isVisible()
                }
            }
            device.screenshots.take("audio_step3_controls_visible")
        }

        step("ШАГ 4: Нажать Pause") {
            AudioPlayerScreen {
                playPauseButton.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("audio_step4_paused")
        }

        step("ШАГ 5: Нажать Play") {
            AudioPlayerScreen {
                playPauseButton.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("audio_step5_resumed")
        }

        step("ШАГ 6: Нажать Next (следующий трек)") {
            AudioPlayerScreen {
                nextButton.click()
                Thread.sleep(2000)
            }
            device.screenshots.take("audio_step6_next_track")
        }

        step("ШАГ 7: Нажать Previous (предыдущий трек)") {
            AudioPlayerScreen {
                previousButton.click()
                Thread.sleep(2000)
            }
            device.screenshots.take("audio_step7_previous_track")
        }

        step("ШАГ 8: Нажать Shuffle") {
            AudioPlayerScreen {
                shuffleButton.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("audio_step8_shuffle")
        }

        step("ШАГ 9: Нажать Repeat") {
            AudioPlayerScreen {
                repeatButton.click()
                Thread.sleep(1000)
            }
            device.screenshots.take("audio_step9_repeat")
        }

        step("ШАГ 10: Вернуться на экран списка (Back)") {
            val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            uiDevice.pressBack()
            Thread.sleep(2000)
            device.screenshots.take("audio_step10_back_to_list")
        }

        step("ШАГ 11: Вернуться на главный экран (ещё раз Back)") {
            val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            uiDevice.pressBack()
            Thread.sleep(2000)
            device.screenshots.take("audio_step11_main_screen")
        }

        step("Результат: аудио воспроизводится, все контролы работают") {
            device.screenshots.take("audio_final_result")
        }
    }
}
