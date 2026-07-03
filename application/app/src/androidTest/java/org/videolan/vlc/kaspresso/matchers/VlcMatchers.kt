package org.videolan.vlc.kaspresso.matchers

import android.view.View
import android.widget.ProgressBar
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * Matches a [ProgressBar] (including [org.videolan.vlc.gui.view.AccessibleSeekBar], which
 * extends it) once its progress value differs from the value observed on the first check.
 * Intended to be polled from inside [com.kaspersky.kaspresso.flakysafety.FlakySafetyProvider.flakySafely],
 * since a single check can't tell "changing" from "not yet observed".
 */
fun isProgressChanging(): Matcher<View> {
    return object : TypeSafeMatcher<View>(ProgressBar::class.java) {
        private var initialProgress = -1

        override fun matchesSafely(view: View): Boolean {
            val progress = (view as ProgressBar).progress
            if (initialProgress == -1) {
                initialProgress = progress
                return false
            }
            return progress != initialProgress
        }

        override fun describeTo(description: Description) {
            description.appendText("a ProgressBar whose progress changes over time")
        }
    }
}
