package org.odk.collect.androidshared.ui

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.R
import org.odk.collect.strings.localization.getLocalizedString

/**
 * Convenience wrapper around Android's [Toast] API.
 */
object ToastUtils {

    lateinit var toaster: Toaster
    private lateinit var lastToast: Toast

    @JvmStatic
    fun showShortToast(message: String) {
        showToast(message)
    }

    @JvmStatic
    fun showShortToast(context: Context, messageResource: Int) {
        showToast(
            context.getLocalizedString(messageResource)
        )
    }

    @JvmStatic
    fun showLongToast(message: String) {
        showToast(message, Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun showLongToast(context: Context, messageResource: Int) {
        showToast(
            context.getLocalizedString(messageResource),
            Toast.LENGTH_LONG
        )
    }

    @JvmStatic
    @Deprecated("Toast position cannot be customized on API 30 and above. A dialog is shown instead for this API levels.")
    fun showShortToastInMiddle(activity: Activity, message: String) {
        showToastInMiddle(activity, message)
    }

    private fun showToast(
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        hideLastToast()
        toaster.toast(message, duration) {
            lastToast = it
        }
    }

    private fun showToastInMiddle(
        activity: Activity,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        if (Build.VERSION.SDK_INT < 30) {
            hideLastToast()
            toaster.toast(message, duration) {
                try {
                    val group = it.view as ViewGroup?
                    val messageTextView = group!!.getChildAt(0) as TextView
                    messageTextView.textSize = 21f
                    messageTextView.gravity = Gravity.CENTER
                } catch (ignored: Exception) {
                    // ignored
                }
                it.setGravity(Gravity.CENTER, 0, 0)
                lastToast = it
            }
        } else {
            MaterialAlertDialogBuilder(activity)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show()
        }
    }

    private fun hideLastToast() {
        if (ToastUtils::lastToast.isInitialized) {
            lastToast.cancel()
        }
    }
}
