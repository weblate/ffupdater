package de.marmaro.krt.ffupdater.app.maintained

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.AppUpdateResult
import de.marmaro.krt.ffupdater.app.AvailableAppVersion
import de.marmaro.krt.ffupdater.app.PackageManagerUtil
import de.marmaro.krt.ffupdater.app.VersionCompareHelper
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.security.FingerprintValidator
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File


abstract class AppBase {
    abstract val packageName: String
    abstract val displayTitle: Int
    abstract val displayDescription: Int
    abstract val displayWarning: Int?
    abstract val displayDownloadSource: Int
    abstract val displayIcon: Int
    abstract val minApiLevel: Int
    abstract val supportedAbis: List<ABI>
    abstract val signatureHash: String

    // The installation does not require special actions like rooting the smartphone
    abstract val normalInstallation: Boolean

    private val mutex = Mutex()

    @AnyThread
    fun isInstalled(context: Context): Boolean {
        return PackageManagerUtil(context.packageManager).isAppInstalled(packageName) &&
                FingerprintValidator(context.packageManager).checkInstalledApp(this).isValid
    }

    @AnyThread
    open fun getInstalledVersion(context: Context): String? {
        return PackageManagerUtil(context.packageManager).getInstalledAppVersionName(packageName)
    }

    @AnyThread
    fun getDisplayInstalledVersion(context: Context): String {
        return context.getString(R.string.installed_version, getInstalledVersion(context))
    }

    @AnyThread
    fun getDisplayAvailableVersion(context: Context, availableVersionResult: AvailableAppVersion): String {
        return context.getString(R.string.available_version, availableVersionResult.version)
    }

    @AnyThread
    open fun appIsInstalled(context: Context, available: AvailableAppVersion) {
    }

    suspend fun checkForUpdateAsync(context: Context): Deferred<AppUpdateResult> {
        return withContext(Dispatchers.IO) {
            async {
                // - use mutex lock to prevent multiple simultaneously update check for a single app
                mutex.withLock {
                    checkForUpdateAndCacheIt(context, useCache = true)
                }
            }
        }
    }

    suspend fun checkForUpdateWithoutCacheAsync(context: Context): Deferred<AppUpdateResult> {
        return withContext(Dispatchers.IO) {
            async {
                // - use mutex lock to prevent multiple simultaneously update check for a single app
                mutex.withLock {
                    checkForUpdateAndCacheIt(context, useCache = false)
                }
            }
        }
    }

    private suspend fun checkForUpdateAndCacheIt(context: Context, useCache: Boolean): AppUpdateResult {

        if (useCache) {
            getUpdateCache(context)
                ?.takeIf { System.currentTimeMillis() - it.timestamp <= CACHE_TIME }
                ?.let { return it }
        }


        val available = checkForUpdate()
        val result = AppUpdateResult(
            availableResult = available,
            isUpdateAvailable = isAvailableVersionHigherThanInstalled(context, available),
            displayVersion = getDisplayAvailableVersion(context, available)
        )
        setUpdateCache(context, result)
        return result
    }

    fun getUpdateCache(context: Context): AppUpdateResult? {
        return try {
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString("${CACHE_KEY_PREFIX}__${packageName}", null)
                ?.let { gson.fromJson(it, AppUpdateResult::class.java) }
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    fun setUpdateCache(context: Context, appUpdateResult: AppUpdateResult) {
        val jsonString = gson.toJson(appUpdateResult)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString("${CACHE_KEY_PREFIX}__${packageName}", jsonString)
            .apply()
    }

    @MainThread
    protected abstract suspend fun checkForUpdate(): AvailableAppVersion

    @MainThread
    open suspend fun isAvailableVersionEqualToArchive(
        context: Context,
        file: File,
        available: AvailableAppVersion
    ): Boolean {
        val archiveVersion = PackageManagerUtil(context.packageManager)
            .getPackageArchiveVersionNameOrNull(file.absolutePath) ?: return false
        return VersionCompareHelper.isAvailableVersionEqual(archiveVersion, available.version)
    }

    @AnyThread
    open fun isAvailableVersionHigherThanInstalled(
        context: Context,
        available: AvailableAppVersion
    ): Boolean {
        val installedVersion = getInstalledVersion(context) ?: return true
        return VersionCompareHelper.isAvailableVersionHigher(installedVersion, available.version)
    }

    companion object {
        const val CACHE_TIME = 600_000L // 10 minutes
        const val CACHE_KEY_PREFIX = "cached_update_check_result__"
        val gson = Gson()
    }
}