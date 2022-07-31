package org.odk.collect.android.support

import androidx.test.espresso.Espresso
import org.odk.collect.androidshared.ui.RecordingToaster

object ToasterAssert {

    @JvmStatic
    fun assertToast(toaster: RecordingToaster, message: String) {
        Espresso.onIdle()
        if (!toaster.popRecordedToasts().stream().anyMatch { s: String -> s == message }) {
            throw RuntimeException("No Toast with text \"$message\" shown on screen!")
        }
    }
}
