package de.marmaro.krt.ffupdater.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.device.DeviceSdkTester

/**
 * Allow the user to select an app from the dialog to install.
 * Show warning or error message (if ABI is not supported) if necessary.
 */
class InstallNewAppDialog(
    private val deviceAbis: List<ABI>,
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val apps = App.values()
            .filter { it.detail.normalInstallation }
            .filterNot { it.detail.isInstalled(context) }


        val names = apps.map { context.getString(it.detail.displayTitle) }.toTypedArray()
        return AlertDialog.Builder(activity)
            .setTitle(R.string.install_new_app)
            .setItems(names) { _, which -> triggerAppInstallation(apps[which]) }
            .create()
    }

    private fun triggerAppInstallation(app: App) {
        // do not install an app which incompatible ABIs
        if (deviceAbis.all { it !in app.detail.supportedAbis }) {
            UnsupportedAbiDialog.newInstance().show(parentFragmentManager)
            return
        }
        // do not install an app which require a newer Android version
        if (DeviceSdkTester.sdkInt < app.detail.minApiLevel) {
            DeviceTooOldDialog.newInstance(app).show(parentFragmentManager)
            return
        }

        if (app.detail.displayWarning != null) {
            AppWarningBeforeInstallationDialog.newInstance(app).show(parentFragmentManager)
            return
        }

        AppInfoBeforeInstallationDialog.newInstance(app).show(parentFragmentManager)
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<TextView>(android.R.id.message)?.movementMethod =
            LinkMovementMethod.getInstance()
    }

    fun show(manager: FragmentManager) {
        show(manager, "install_new_app_dialog")
    }

    companion object {
        fun newInstance(deviceAbis: List<ABI>): InstallNewAppDialog {
            return InstallNewAppDialog(deviceAbis)
        }
    }
}