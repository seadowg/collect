package org.getodk.toaster.espresso

import androidx.test.espresso.Espresso
import org.getodk.toaster.RecordingToaster

object ToasterAssert {

    @JvmStatic
    fun assertToast(toaster: RecordingToaster, message: String) {
        Espresso.onIdle()
        if (!toaster.popRecordedToasts().any { s: String -> s == message }) {
            throw RuntimeException("No Toast with text \"$message\" shown on screen!")
        }
    }
}
