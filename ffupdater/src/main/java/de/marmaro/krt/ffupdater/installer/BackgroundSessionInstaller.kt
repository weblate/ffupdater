package de.marmaro.krt.ffupdater.installer

import android.content.Context
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import java.io.File

@RequiresApi(Build.VERSION_CODES.N)
class BackgroundSessionInstaller(
    context: Context,
    app: App,
    file: File
) : BackgroundAppInstaller, SessionInstallerBase(context, app, file) {

    init {
        registerIntentReceiver()
    }

    override fun getIntentNameForAppInstallationCallback(): String {
        return "de.marmaro.krt.ffupdater.installer.BackgroundSessionInstaller.app_installed"
    }

    override fun requestInstallationPermission(bundle: Bundle) {
        val status = bundle.getInt(PackageInstaller.EXTRA_STATUS)
        failure(status, R.string.session_installer__require_user_interaction)
    }

    override fun close() {
        unregisterIntentReceiver()
    }

}