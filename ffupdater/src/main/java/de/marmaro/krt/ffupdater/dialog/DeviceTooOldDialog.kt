package de.marmaro.krt.ffupdater.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.device.DeviceSdkTester
import de.marmaro.krt.ffupdater.utils.AndroidVersionCodes

/**
 * Show the user that the app could not be installed because the operating system is too old.
 */
class DeviceTooOldDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val app = App.valueOf(requireNotNull(requireArguments().getString(BUNDLE_APP_NAME)) {
            "$BUNDLE_APP_NAME is not set."
        })
        val required = AndroidVersionCodes.getVersionForApiLevel(app.detail.minApiLevel)
        val actual = AndroidVersionCodes.getVersionForApiLevel(DeviceSdkTester.sdkInt)
        return AlertDialog.Builder(activity)
                .setTitle(R.string.device_too_old_dialog__title)
                .setMessage(getString(R.string.device_too_old_dialog__message, required, actual))
                .setNegativeButton(R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
    }

    fun show(manager: FragmentManager) {
        show(manager, "device_too_old_dialog")
    }

    companion object {
        private const val BUNDLE_APP_NAME = "app_name"

        fun newInstance(app: App): DeviceTooOldDialog {
            val bundle = Bundle()
            bundle.putString(BUNDLE_APP_NAME, app.name)
            val fragment = DeviceTooOldDialog()
            fragment.arguments = bundle
            return fragment
        }
    }
}