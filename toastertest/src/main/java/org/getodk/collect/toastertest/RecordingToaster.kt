package org.getodk.collect.toastertest

import android.widget.Toast
import org.odk.collect.toaster.Toaster

class RecordingToaster : Toaster {
    private var toasts = mutableListOf<String>()

    override fun toast(message: String, duration: Int, beforeShow: ((Toast) -> Unit)?) {
        toasts.add(message)
    }

    fun popRecordedToasts(): List<String> {
        val copy = toasts.toList()
        toasts.clear()

        return copy
    }
}
