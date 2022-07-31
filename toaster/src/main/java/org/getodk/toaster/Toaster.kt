package org.getodk.toaster

import android.app.Application
import android.widget.Toast

interface Toaster {
    fun toast(
        message: String,
        duration: Int = Toast.LENGTH_SHORT,
        beforeShow: ((Toast) -> Unit)? = null,
    )
}

class ApplicationToaster(private val application: Application) : Toaster {

    override fun toast(message: String, duration: Int, beforeShow: ((Toast) -> Unit)?) {
        val toast = Toast.makeText(application, message, duration)
        if (beforeShow != null) {
            beforeShow(toast)
        }

        toast.show()
    }
}
