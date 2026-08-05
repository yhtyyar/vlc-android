package org.videolan.vlc.kaspresso

import android.os.Build
import android.os.PowerManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingPolicies
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.videolan.resources.util.startMedialibrary
import org.videolan.tools.KEY_SHOW_UPDATE
import org.videolan.tools.Settings
import org.videolan.tools.putSingle
import org.videolan.vlc.gui.MainActivity
import org.videolan.vlc.util.TestCoroutineContextProvider
import java.util.concurrent.TimeUnit

/**
 * Базовый класс для всех Kaspresso UI-тестов VLC Android.
 *
 * Философия:
 *  - Каждый тест начинается ЧЕЛОВЕЧЕСКИ: приложение может быть запущено через UI Automator
 *    (имитация того, как запускает приложение обычный пользователь)
 *  - Перед каждым тестом устройство настраивается:
 *      * Яркость на максимум для визуальной наглядности
 *      * Экран не гаснет (таймаут отключён)
 *      * Включен show_touches (видны клики на экране)
 *  - После каждого теста устройство возвращается в исходное состояние
 *  - Медиабиблиотека запускается и ждёт завершения сканирования
 *  - Onboarding пропускается если он появился
 */
@RunWith(AndroidJUnit4::class)
abstract class KaspressoUITest : TestCase(KaspressoConfig.builder) {

    companion object {
        /** Пакет приложения VLC */
        const val VLC_PACKAGE = "org.videolan.vlc.debug"

        /** Имя приложения в лаунчере */
        const val VLC_APP_NAME = "VLC"

        private fun buildPermissionList(): Array<String> {
            val permissions = mutableListOf("android.permission.READ_EXTERNAL_STORAGE")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add("android.permission.READ_MEDIA_VIDEO")
                permissions.add("android.permission.READ_MEDIA_AUDIO")
                permissions.add("android.permission.POST_NOTIFICATIONS")
            }
            return permissions.toTypedArray()
        }
    }

    /** ======== JUnit Rules ======== */

    /** Разрешения — внешний rule */
    @get:Rule(order = 0)
    val storagePermissionGrant: GrantPermissionRule =
        GrantPermissionRule.grant(*buildPermissionList())

    /** Отключаем диалог обновления nightly build */
    @get:Rule(order = 1)
    val skipNightlyUpdateDialog = object : ExternalResource() {
        override fun before() {
            Settings.getInstance(context).putSingle(KEY_SHOW_UPDATE, false)
        }
    }

    /** Activity Rule — для совместимости с экранами. */
    @get:Rule(order = 2)
    open val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /** ======== Common properties ======== */

    protected val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    private val wakeLock: PowerManager.WakeLock by lazy {
        val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
        pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "vlc:kaspresso:keepScreenOn"
        ).apply { setReferenceCounted(false) }
    }

    /** ======== Lifecycle ======== */

    @Before
    fun baseSetUp() {
        // Таймауты для ожидания — щедрые, т.к. мы не гонимся за скоростью
        IdlingPolicies.setMasterPolicyTimeout(3, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(3, TimeUnit.MINUTES)

        // WakeLock чтобы экран не гас
        @Suppress("DEPRECATION")
        if (!wakeLock.isHeld) {
            wakeLock.acquire(15 * 60 * 1000L)
        }

        // Запускаем медиабиблиотеку (фоновое сканирование медиа)
        context.startMedialibrary(coroutineContextProvider = TestCoroutineContextProvider())

        // Настройка устройства для визуальной наглядности
        setupDeviceForVisualization()

        // Пропускаем onboarding если он есть
        skipOnboardingIfPresent()

        // Хук для дочерних классов
        beforeTest()
    }

    @After
    fun baseTearDown() {
        @Suppress("DEPRECATION")
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        restoreDeviceSettings()
    }

    /** ======== Device Setup / Visualization ======== */

    /**
     * Настраивает устройство для визуальной наглядности:
     *  - Максимальная яркость экрана
     *  - Таймаут экрана отключён (никогда не гаснет)
     *  - Show touches включён (видны клики на экране)
     *  - Pointer location — след за пальцем
     */
    private fun setupDeviceForVisualization() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            device.executeShellCommand("settings put system screen_brightness 255")
            device.executeShellCommand("settings put system screen_off_timeout 1800000")
            device.executeShellCommand("settings put system show_touches 1")
            device.executeShellCommand("settings put system pointer_location 1")
        } catch (e: Exception) {
            println("[KaspressoUITest] Не удалось настроить устройство: ${e.message}")
        }
    }

    private fun restoreDeviceSettings() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            device.executeShellCommand("settings put system show_touches 0")
            device.executeShellCommand("settings put system pointer_location 0")
        } catch (e: Exception) {
            // Игнорируем
        }
    }

    /** ======== App Launch Methods ======== */

    /**
     * Запускает приложение VLC через UI Automator — как это делает обычный пользователь:
     *  1. Свайп вверх (доступ к приложениям)
     *  2. Поиск иконки VLC
     *  3. Клик по иконке
     *  4. Ожидание загрузки главного экрана
     *
     * Этот метод НЕ использует step()/flakySafely() — он предназначен для вызова
     * внутри блока run { step(...) { launchAppFromHomeScreen() } }
     */
    protected fun launchAppFromHomeScreen() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // ШАГ 1: Вернуться на домашний экран
        device.pressHome()
        device.waitForIdle(3000)
        // Скриншот делается в вызывающем step() через device.screenshots.take()

        // ШАГ 2: Открыть список приложений (свайп вверх)
        val screenHeight = device.displayHeight
        val screenWidth = device.displayWidth
        device.swipe(screenWidth / 2, screenHeight - 100, screenWidth / 2, screenHeight / 3, 20)
        device.waitForIdle(3000)

        // ШАГ 3: Найти и кликнуть по иконке VLC
        val vlcIcon = device.findObject(UiSelector().text(VLC_APP_NAME))
        if (vlcIcon != null && vlcIcon.exists()) {
            vlcIcon.click()
        } else {
            val appIcon = device.findObject(
                UiSelector().descriptionContains("VLC")
                    .className("android.widget.TextView")
            )
            if (appIcon != null && appIcon.exists()) {
                appIcon.click()
            } else {
                device.executeShellCommand(
                    "am start -n $VLC_PACKAGE/org.videolan.vlc.gui.MainActivity"
                )
            }
        }
        device.waitForIdle(3000)

        // ШАГ 4: Ожидание загрузки главного экрана VLC
        Thread.sleep(3000)

        // ШАГ 5: Проверка что табы навигации видны
        val videoTab = device.findObject(
            UiSelector()
                .text("Video")
                .className("android.widget.TextView")
                .resourceIdMatches(".*navigation_bar_item_large_label_view")
        )
        if (videoTab == null || !videoTab.exists()) {
            throw AssertionError("Главный экран VLC не загрузился вовремя")
        }
        device.waitForIdle(2000)
    }

    /**
     * Альтернативный запуск: если приложение уже открыто, просто проверяем
     * что мы на главном экране. Если нет — запускаем через launchAppFromHomeScreen().
     */
    protected fun ensureAppIsOpen() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val videoTab = device.findObject(
            UiSelector().text("Video").className("android.widget.TextView")
        )
        if (videoTab == null || !videoTab.exists()) {
            launchAppFromHomeScreen()
        } else {
            videoTab.click()
            device.waitForIdle(2000)
        }
        // Every test starts with this call, and on some runs VLC's own storage-detection
        // shows a modal "New external storage detected" dialog right as the grid becomes
        // interactive. Espresso doesn't know about that separate dialog window, so a click
        // aimed at a grid item underneath silently lands on/is absorbed by the dialog
        // instead — reproduced for real: the grid stayed untouched and no player ever
        // opened. Dismiss it here so every test starts from a clean, dialog-free screen.
        dismissTransientDialogsIfPresent()
    }

    /** ======== Helper Methods ======== */

    /**
     * Пропускает onboarding wizard если он появился.
     */
    private fun skipOnboardingIfPresent() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val skip = device.findObject(UiSelector().text("SKIP").className("android.widget.Button"))
        if (skip != null && skip.exists() && skip.isEnabled) {
            skip.click()
            device.waitForIdle(3000)
        }
    }

    /**
     * Закрывает переходные диалоги, которые могут появиться ПОСЛЕ настоящего запуска через
     * иконку лаунчера — Welcome-визард onboarding'а и debug-диалог "Auto update" (nightly).
     * Нужен отдельно от [skipOnboardingIfPresent]/baseSetUp: тот вызывается один раз в
     * @Before, сразу после запуска MainActivity через ActivityScenarioRule, который запускает
     * Activity напрямую через Instrumentation и никогда не показывает ни один из этих
     * диалогов. Если тест сам эмулирует "человеческий" запуск через иконку в лаунчере
     * (см. [launchAppFromHomeScreen]), эти диалоги встречаются именно на ЭТОМ запуске — уже
     * после того как @Before отработал, поэтому их нужно проверять повторно.
     */
    protected fun dismissTransientDialogsIfPresent(maxAttempts: Int = 5) {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        repeat(maxAttempts) {
            val skip = device.findObject(UiSelector().text("SKIP").className("android.widget.Button"))
            if (skip != null && skip.exists() && skip.isEnabled) {
                skip.click()
                device.waitForIdle(1500)
                return@repeat
            }
            val no = device.findObject(UiSelector().text("NO").className("android.widget.Button"))
            if (no != null && no.exists() && no.isEnabled) {
                no.click()
                device.waitForIdle(1500)
                return@repeat
            }
            Thread.sleep(400)
        }
    }

    /**
     * Шаблонный метод. Дочерние классы могут переопределить для
     * собственной подготовительной логики ПОСЛЕ настройки устройства.
     */
    protected open fun beforeTest() {}
}
