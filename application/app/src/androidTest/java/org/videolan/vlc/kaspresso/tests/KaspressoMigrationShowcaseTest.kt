package org.videolan.vlc.kaspresso.tests

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.kotlin.Description
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.Severity
import io.qameta.allure.kotlin.SeverityLevel
import io.qameta.allure.kotlin.Step
import io.qameta.allure.kotlin.Story
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.videolan.medialibrary.interfaces.Medialibrary
import org.videolan.medialibrary.interfaces.media.Playlist
import org.videolan.vlc.R
import org.videolan.vlc.gui.MainActivity
import org.videolan.vlc.kaspresso.KaspressoUITest
import org.videolan.vlc.kaspresso.screens.MainScreen
import org.videolan.vlc.kaspresso.screens.PlaylistScreen
import org.videolan.vlc.kaspresso.utils.TestMediaProvider
import org.videolan.vlc.sizeOfAtLeast
import org.videolan.vlc.withRecyclerView

/**
 * A worked side-by-side migration of one real, existing Espresso test —
 * [org.videolan.vlc.gui.PlaylistFragmentUITest.whenOnePlaylist_checkContextMenuWorks] — to
 * Kaspresso, kept next to it rather than replacing it (see the PR description for why this
 * specific test was picked and what it's meant to demonstrate). The original is untouched; this
 * class duplicates its setup (`createDummyPlaylist`-equivalent below) rather than reusing the
 * original's private helper, since nothing here modifies that file.
 *
 * Two real corrections to how this task was originally described: Allure Kotlin has no `@Title`
 * annotation (only [Description] comes close) — confirmed by inspecting the actual
 * `allure-kotlin-commons` jar, not assumed. And [Step] does exist, but Java/JVM Allure's
 * annotation-based step interception normally relies on AspectJ bytecode weaving that isn't
 * configured for this Android module; applying it below is safe (it's a real, valid annotation
 * that won't break compilation) but shouldn't be presented as guaranteed to produce a nested
 * Allure step without that additional setup. This suite's actual step reporting comes from
 * Kaspresso's own `step { }` DSL, bridged to Allure by `kaspresso-allure-support`'s
 * `AllureMapperStepInterceptor` (real, confirmed via bytecode when this framework was first set
 * up — see ANALYSIS.md section 2).
 */
@Epic("VLC Android")
@Feature("Playlists")
class KaspressoMigrationShowcaseTest : KaspressoUITest() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun pushSampleAudioAndCreateDummyPlaylist() {
        // Espresso: the original test creates its dummy playlist from whatever audio the
        // device/emulator already happens to have indexed, with no fallback if there's none.
        // Kaspresso here reuses this suite's TestMediaProvider (already built for
        // AudioPlaybackTest) so the playlist reliably has real content, and Assume.assumeTrue
        // turns "nothing indexed" into a clean skip instead of a confusing failure deep in the
        // test body.
        TestMediaProvider.pushAudio()
        TestMediaProvider.rescanAndAwait()

        val medialibrary = Medialibrary.getInstance()
        val audio = medialibrary.getPagedAudio(Medialibrary.SORT_DEFAULT, false, true, false, 5, 0)
        assumeTrue("No audio indexed by the medialibrary in time — skipping", audio.isNotEmpty())

        medialibrary.createPlaylist(DUMMY_PLAYLIST_NAME, true, false)
                .append(audio.map { it.id })
    }

    @After
    fun deleteDummyPlaylist() {
        Medialibrary.getInstance().getPlaylists(Playlist.Type.All, false).forEach { it.delete() }
    }

    @Test
    @Story("Context menu")
    @Severity(SeverityLevel.NORMAL)
    @Description("Migrated from org.videolan.vlc.gui.PlaylistFragmentUITest.whenOnePlaylist_checkContextMenuWorks")
    fun contextMenuListsAllExpectedActions() = run {
        step("Open the Playlists tab") {
            MainScreen { playlistsTab.click() }
        }

        // Espresso (original): Thread.sleep(1500) — a fixed delay that either wastes time when
        // the list is already ready, or isn't long enough under load, with no signal either way.
        // Kaspresso: flakySafely polls the real condition (the list actually has an item) and
        // returns as soon as it's true, with a bounded retry window instead of a guess.
        step("Wait for the dummy playlist's track to appear") {
            flakySafely { PlaylistScreen { audioList.isVisible() } }
        }

        openTheItemsContextMenu()

        step("The context menu lists all expected actions") {
            // These specific assertions are unchanged from the original Espresso test — Kaspresso
            // doesn't need to reinvent hasDescendant/withText or this project's own
            // sizeOfAtLeast() matcher, it just organizes the flow that leads up to them.
            flakySafely {
                onView(withId(R.id.ctx_list))
                        .check(matches(isDisplayed()))
                        .check(matches(sizeOfAtLeast(5)))
                        .check(matches(hasDescendant(withText(R.string.play))))
                        .check(matches(hasDescendant(withText(R.string.append))))
                        .check(matches(hasDescendant(withText(R.string.insert_next))))
                        .check(matches(hasDescendant(withText(R.string.add_to_playlist))))
                        .check(matches(hasDescendant(withText(R.string.delete))))
            }
            device.screenshots.take("playlist_context_menu")
        }
    }

    // Espresso: withRecyclerView(R.id.audio_list).atPositionOnView(0, R.id.item_more) is this
    // project's own existing helper (org.videolan.vlc.UtilViewMatchers) — reused as-is here rather
    // than reimplemented, since Kaspresso is a layer on top of Espresso, not a replacement for it.
    @Step("Open the first playlist item's context menu")
    private fun openTheItemsContextMenu() {
        onView(withRecyclerView(R.id.audio_list).atPositionOnView(0, R.id.item_more))
                .perform(click())
    }

    private companion object {
        const val DUMMY_PLAYLIST_NAME = "kaspresso_showcase_test"
    }
}
