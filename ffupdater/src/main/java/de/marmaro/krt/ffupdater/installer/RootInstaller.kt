package de.marmaro.krt.ffupdater.installer

import com.topjohnwu.superuser.Shell
import de.marmaro.krt.ffupdater.installer.AppInstaller.InstallResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern

/**
 * Copied from https://gitlab.com/AuroraOSS/AuroraStore/-/blob/master/app/src/main/java/com/aurora/store/data/installer/RootInstaller.kt
 */
class RootInstaller(private val file: File) : AppInstaller {
    private val status = CompletableDeferred<InstallResult>()

    override suspend fun installAsync(): Deferred<InstallResult> {
        withContext(Dispatchers.IO) {
            install()
        }
        return status
    }

    private fun install() {
        if (!Shell.getShell().isRoot) {
            status.complete(InstallResult(false, -90, "Missing root access."))
            return
        }

        val totalSize = file.length().toInt()
        val response = Shell.cmd("pm install-create -i com.android.vending --user 0 -r -S $totalSize")
            .exec()
            .out

        val sessionIdPattern = Pattern.compile("(\\d+)")
        val sessionIdMatcher = sessionIdPattern.matcher(response[0])
        val found = sessionIdMatcher.find()
        if (!found) {
            status.complete(InstallResult(false, -91, "Could not find session ID."))
            return
        }

        val sessionId = sessionIdMatcher.group(1)?.toInt()
        Shell.cmd("cat \"${file.absoluteFile}\" | pm install-write -S ${file.length()} $sessionId \"${file.name}\"")
            .exec()
        val shellResult = Shell.cmd("pm install-commit $sessionId")
            .exec()
        status.complete(InstallResult(shellResult.isSuccess, null, shellResult.out.joinToString(";")))
    }
}
