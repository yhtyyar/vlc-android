package org.videolan.vlc.kaspresso

import com.kaspersky.components.alluresupport.interceptors.step.AllureMapperStepInterceptor
import com.kaspersky.components.alluresupport.withAllureSupport
import com.kaspersky.kaspresso.interceptors.watcher.testcase.impl.composite.TestRunCompositeWatcherInterceptor
import com.kaspersky.kaspresso.interceptors.watcher.view.impl.logging.LoggingViewActionWatcherInterceptor
import com.kaspersky.kaspresso.interceptors.watcher.view.impl.logging.LoggingViewAssertionWatcherInterceptor
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.logger.UiTestLogger

/**
 * Конфигурация Kaspresso для профессионального UI-тестирования VLC Android.
 *
 * Особенности:
 *  - Визуализация кликов (show clicks) — чтобы тестировщик видел где кликает
 *  - Автоматические скриншоты перед и после каждого шага
 *  - Подробное логирование всех действий и проверок
 *  - Allure-отчёты для каждого теста
 */
object KaspressoConfig {

    val builder: Kaspresso.Builder = Kaspresso.Builder.withAllureSupport {
        /**
         * Включаем визуальное отображение кликов на экране (show touches).
         * При каждом тапе на экране появляется кружок — тестировщик видит где тест кликает.
         */
        beforeEachTest {
            // Включаем show touches через ADB
            device.uiDevice.executeShellCommand("settings put system show_touches 1")
            // Увеличиваем время анимации для визуальной наглядности
            device.uiDevice.executeShellCommand("settings put global window_animation_scale 1.0")
            device.uiDevice.executeShellCommand("settings put global transition_animation_scale 1.0")
            device.uiDevice.executeShellCommand("settings put global animator_duration_scale 1.0")
        }

        afterEachTest {
            // Отключаем show touches
            try {
                device.uiDevice.executeShellCommand("settings put system show_touches 0")
            } catch (e: Exception) {
                // Игнорируем ошибку если устройство отключилось
            }
        }
    }
}
