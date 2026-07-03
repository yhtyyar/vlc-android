package org.videolan.vlc.kaspresso.utils

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.videolan.medialibrary.interfaces.Medialibrary
import java.io.File

/**
 * Pushes small, real, checked-in sample media (application/app/src/androidTest/assets/media/)
 * onto the device so playback tests exercise real content instead of skipping via Assume.
 *
 * Reads the bundled assets via `InstrumentationRegistry.getInstrumentation().context` (the
 * androidTest APK's own context) — only it has these assets. Confirmed for real on a
 * Pixel_7a/API 35 emulator: that same context's `getExternalFilesDir(null)` returns null (its
 * per-package external storage sandbox isn't provisioned, likely because the test APK is never
 * "launched" as an app in its own right), and `File(null, name)` silently falls back to a
 * relative path resolving under `/`, which is read-only (EROFS) — so the local staging copy uses
 * `targetContext` (the app under test) instead, which is guaranteed to have one since it's the
 * app actively running throughout the test. A shell `cp` (shell UID bypasses scoped-storage
 * sandboxing) then moves it somewhere VLC's medialibrary scans.
 */
object TestMediaProvider {

    private const val ASSET_DIR = "media"
    private const val DEVICE_DIR = "/sdcard/Movies/kaspresso_test_media"

    fun pushVideo(): File = pushAsset("sample_video.mp4")

    // Two distinct tracks so "skip to next" tests have somewhere real to go.
    fun pushAudio(): List<File> = listOf(pushAsset("sample_audio.mp3"), pushAsset("sample_audio_2.mp3"))

    /**
     * Forces a medialibrary rescan and blocks until it finishes (bounded — a full rescan walks
     * the whole device storage, and on a long-lived device/emulator that's accumulated many prior
     * Kaspresso test-run artifacts under /storage/emulated/0/Documents/, that walk has been
     * observed taking minutes; a fresh CI emulator doesn't have this problem, but this bound keeps
     * a slow scan from ever hanging the whole suite the way an unbounded wait did here).
     */
    fun rescanAndAwait(maxWaitMs: Long = 30_000) {
        val medialibrary = Medialibrary.getInstance()
        medialibrary.forceRescan()
        val deadline = System.currentTimeMillis() + maxWaitMs
        while (medialibrary.isWorking && System.currentTimeMillis() < deadline) {
            Thread.sleep(200)
        }
    }

    private fun pushAsset(assetName: String): File {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)

        val localCopy = File(instrumentation.targetContext.getExternalFilesDir(null), assetName)
        instrumentation.context.assets.open("$ASSET_DIR/$assetName").use { input ->
            localCopy.outputStream().use { output -> input.copyTo(output) }
        }

        device.executeShellCommand("mkdir -p $DEVICE_DIR")
        val devicePath = "$DEVICE_DIR/$assetName"
        device.executeShellCommand("cp ${localCopy.absolutePath} $devicePath")
        return File(devicePath)
    }
}
