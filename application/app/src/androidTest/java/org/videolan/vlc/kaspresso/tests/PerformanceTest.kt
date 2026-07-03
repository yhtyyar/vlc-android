package org.videolan.vlc.kaspresso.tests

import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Story
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.videolan.vlc.gui.MainActivity
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.MainScreen

/**
 * Wall-clock timings on a shared CI/emulator runner are inherently noisy, so thresholds here are
 * deliberately generous (catching a real regression, e.g. a multi-second hang, not micro-budgets).
 */
@Epic("VLC Android")
@Feature("Performance")
class PerformanceTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    @Story("Startup")
    @Severity(SeverityLevel.NORMAL)
    fun theBottomNavigationAppearsWithinAReasonableTime() = run {
        val startTime = System.currentTimeMillis()

        step("Wait for the bottom navigation to appear") {
            flakySafely {
                MainScreen { videoTab.isVisible() }
            }
        }

        step("Check the elapsed time") {
            val elapsedMs = System.currentTimeMillis() - startTime
            device.screenshots.take("performance_startup_${elapsedMs}ms")
            assertTrue("Bottom navigation took ${elapsedMs}ms to appear", elapsedMs < 10_000)
        }
    }

    @Test
    @Story("Navigation")
    @Severity(SeverityLevel.NORMAL)
    fun switchingTabsRepeatedlyStaysResponsive() = run {
        val iterations = 5
        val times = mutableListOf<Long>()

        step("Switch between the video and audio tabs $iterations times") {
            repeat(iterations) {
                val start = System.currentTimeMillis()
                MainScreen {
                    audioTab.click()
                    flakySafely { audioTab.isSelected() }
                    videoTab.click()
                    flakySafely { videoTab.isSelected() }
                }
                times.add(System.currentTimeMillis() - start)
            }
        }

        step("Check the average round-trip time") {
            val averageMs = times.average()
            assertTrue("Average tab switch round-trip took ${averageMs}ms", averageMs < 3_000)
        }
    }
}
