package de.marmaro.krt.ffupdater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import de.marmaro.krt.ffupdater.InstallActivity.State.*
import de.marmaro.krt.ffupdater.R.id.install_activity__exception__show_button
import de.marmaro.krt.ffupdater.R.string.crash_report__explain_text__install_activity_fetching_url
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.UpdateCheckResult
import de.marmaro.krt.ffupdater.app.impl.exceptions.GithubRateLimitExceededException
import de.marmaro.krt.ffupdater.app.impl.exceptions.NetworkException
import de.marmaro.krt.ffupdater.crash.CrashListener
import de.marmaro.krt.ffupdater.download.*
import de.marmaro.krt.ffupdater.installer.AppInstaller
import de.marmaro.krt.ffupdater.security.FingerprintValidator
import de.marmaro.krt.ffupdater.security.FingerprintValidator.CertificateValidationResult
import de.marmaro.krt.ffupdater.settings.SettingsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Activity for downloading and installing apps on devices with API Level >= 24/Nougat.
 * Reason: If have to use the DownloadManager because this is the easiest way to download the app and access it with
 * the scheme format (for example: content://downloads/all_downloads/20).
 * The DownloadManager is more difficult to use then the default java way, but the DownloadManager offers more features
 * like restarting downloads, showing the current download status etc.
 */
class InstallActivity : AppCompatActivity() {
    private lateinit var viewModel: InstallActivityViewModel
    private lateinit var fingerprintValidator: FingerprintValidator
    private lateinit var appInstaller: AppInstaller
    private lateinit var appCache: AppCache
    private lateinit var settingsHelper: SettingsHelper

    // necessary for communication with State enums
    private lateinit var app: App
    private var state = SUCCESS_PAUSE
    private var stateJob: Job? = null
    private lateinit var fileFingerprint: CertificateValidationResult
    private lateinit var appFingerprint: CertificateValidationResult
    private var appInstallationFailedErrorMessage: String? = null

    // persistent data across orientation changes
    class InstallActivityViewModel : ViewModel() {
        var app: App? = null
        var fileDownloader: FileDownloader? = null
        var updateCheckResult: UpdateCheckResult? = null
        var fetchUrlException: Exception? = null
        var fetchUrlExceptionText: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.install_activity)
        CrashListener.openCrashReporterForUncaughtExceptions(this)
        AppCompatDelegate.setDefaultNightMode(SettingsHelper(this).themePreference)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val passedAppName = intent.extras?.getString(EXTRA_APP_NAME)
        if (passedAppName == null) {
            // InstallActivity was unintentionally started again after finishing the download
            finish()
            return
        }
        app = App.valueOf(passedAppName)

        settingsHelper = SettingsHelper(this)
        fingerprintValidator = FingerprintValidator(packageManager)
        appInstaller = AppInstaller.create(
            successfulInstallationCallback = {
                restartStateMachine(USER_HAS_INSTALLED_APP_SUCCESSFUL)
            },
            failedInstallationCallback = { errorMessage ->
                if (errorMessage != null) {
                    appInstallationFailedErrorMessage = errorMessage
                }
                restartStateMachine(FAILURE_APP_INSTALLATION)
            })
        appCache = AppCache(app)
        findViewById<View>(R.id.install_activity__retrigger_installation__button).setOnClickListener {
            restartStateMachine(TRIGGER_INSTALLATION_PROCESS)
        }
        findViewById<Button>(R.id.install_activity__delete_cache_button).setOnClickListener {
            appCache.delete(this)
            hide(R.id.install_activity__delete_cache)
        }
        findViewById<Button>(R.id.install_activity__open_cache_folder_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val parentFolder = appCache.getFile(this).parentFile ?: return@setOnClickListener
            val uri = Uri.parse("file://${parentFolder.absolutePath}")
            intent.setDataAndType(uri, "resource/folder")
            startActivity(Intent.createChooser(intent, "Open folder"))
        }

        // make sure that the ViewModel is correct for the current app
        viewModel = ViewModelProvider(this).get(InstallActivityViewModel::class.java)
        if (viewModel.app != null) {
            check(viewModel.app == app)
        }
        viewModel.app = app
        // only start new download if no download is still running (can happen after rotation)
        restartStateMachine(START)
    }

    override fun onStop() {
        super.onStop()
        // finish activity when it's finished and the user switch to another app
        if (state == SUCCESS_STOP || state == ERROR_STOP) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stateJob?.cancel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        appInstaller.onActivityResult(requestCode, resultCode, data)
    }

    private fun restartStateMachine(jumpDestination: State) {
        // security check to prevent a illegal restart
        if (state != SUCCESS_PAUSE) {
            return
        }
        stateJob?.cancel()
        state = jumpDestination
        stateJob = lifecycleScope.launch(Dispatchers.Main) {
            while (state != ERROR_STOP
                && state != SUCCESS_PAUSE
                && state != SUCCESS_STOP
            ) {
                state = state.action(this@InstallActivity)
            }
        }
    }

    /**
     * This method will be called when the app installation is completed.
     * @param intent intent
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        appInstaller.onNewIntentCallback(intent, this)
    }

    private fun show(viewId: Int) {
        findViewById<View>(viewId).visibility = View.VISIBLE
    }

    private fun hide(viewId: Int) {
        findViewById<View>(viewId).visibility = View.GONE
    }

    private fun setText(textId: Int, text: String) {
        findViewById<TextView>(textId).text = text
    }

    @MainThread
    enum class State(val action: suspend (InstallActivity) -> State) {
        START(InstallActivity::start),
        CHECK_IF_STORAGE_IS_MOUNTED(InstallActivity::checkIfStorageIsMounted),
        CHECK_FOR_ENOUGH_STORAGE(InstallActivity::checkForEnoughStorage),
        PRECONDITIONS_ARE_CHECKED(InstallActivity::fetchDownloadInformation),
        START_DOWNLOAD(InstallActivity::startDownload),
        REUSE_CURRENT_DOWNLOAD(InstallActivity::reuseCurrentDownload),
        DOWNLOAD_WAS_SUCCESSFUL(InstallActivity::downloadWasSuccessful),
        USE_CACHED_DOWNLOADED_APK(InstallActivity::useCachedDownloadedApk),
        FINGERPRINT_OF_DOWNLOADED_FILE_OK(InstallActivity::fingerprintOfDownloadedFileOk),
        TRIGGER_INSTALLATION_PROCESS(InstallActivity::triggerInstallationProcess),
        USER_HAS_INSTALLED_APP_SUCCESSFUL(InstallActivity::userHasInstalledAppSuccessful),
        APP_INSTALLATION_HAS_BEEN_REGISTERED(InstallActivity::appInstallationHasBeenRegistered),
        FINGERPRINT_OF_INSTALLED_APP_OK(InstallActivity::fingerprintOfInstalledAppOk),

        //===============================================

        FAILURE_UNKNOWN_SIGNATURE_OF_INSTALLED_APP(InstallActivity::failureUnknownSignatureOfInstalledApp),
        FAILURE_EXTERNAL_STORAGE_NOT_ACCESSIBLE(InstallActivity::failureExternalStorageNotAccessible),
        FAILURE_DOWNLOAD_UNSUCCESSFUL(InstallActivity::failureDownloadUnsuccessful),
        FAILURE_INVALID_FINGERPRINT_OF_DOWNLOADED_FILE(InstallActivity::failureInvalidFingerprintOfDownloadedFile),
        FAILURE_APP_INSTALLATION(InstallActivity::failureAppInstallation),
        FAILURE_FINGERPRINT_OF_INSTALLED_APP_INVALID(InstallActivity::failureFingerprintOfInstalledAppInvalid),
        FAILURE_SHOW_FETCH_URL_EXCEPTION(InstallActivity::failureShowFetchUrlException),

        // SUCCESS_PAUSE => state machine will be restarted externally
        SUCCESS_PAUSE({ SUCCESS_PAUSE }),
        SUCCESS_STOP({ SUCCESS_STOP }),
        ERROR_STOP({ ERROR_STOP });
    }

    companion object {
        const val EXTRA_APP_NAME = "app_name"

        @MainThread
        suspend fun start(ia: InstallActivity): State {
            if (!ia.app.detail.isInstalled(ia) || ia.fingerprintValidator.checkInstalledApp(ia.app).isValid) {
                return CHECK_IF_STORAGE_IS_MOUNTED
            }
            return FAILURE_UNKNOWN_SIGNATURE_OF_INSTALLED_APP
        }

        @MainThread
        fun checkIfStorageIsMounted(ia: InstallActivity): State {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                return CHECK_FOR_ENOUGH_STORAGE
            }
            return FAILURE_EXTERNAL_STORAGE_NOT_ACCESSIBLE
        }

        @MainThread
        fun checkForEnoughStorage(ia: InstallActivity): State {
            if (StorageUtil.isEnoughStorageAvailable()) {
                return PRECONDITIONS_ARE_CHECKED
            }
            ia.show(R.id.tooLowMemory)
            val mbs = StorageUtil.getFreeStorageInMebibytes()
            val message = ia.getString(R.string.install_activity__too_low_memory_description, mbs)
            ia.setText(R.id.tooLowMemoryDescription, message)
            return PRECONDITIONS_ARE_CHECKED
        }

        @MainThread
        suspend fun fetchDownloadInformation(ia: InstallActivity): State {
            val app = ia.app
            ia.show(R.id.fetchUrl)
            val downloadSource = ia.getString(app.detail.displayDownloadSource)
            val runningText = ia.getString(
                R.string.install_activity__fetch_url_for_download,
                downloadSource
            )
            ia.setText(R.id.fetchUrlTextView, runningText)

            // check if network type requirements are met
            if (!ia.settingsHelper.isForegroundUpdateCheckOnMeteredAllowed &&
                NetworkUtil.isActiveNetworkMetered(ia)
            ) {
                ia.viewModel.fetchUrlExceptionText =
                    ia.getString(R.string.main_activity__no_unmetered_network)
                return FAILURE_SHOW_FETCH_URL_EXCEPTION
            }

            val updateCheckResult = try {
                app.detail.updateCheck(ia)
            } catch (e: GithubRateLimitExceededException) {
                ia.viewModel.fetchUrlException = e
                ia.viewModel.fetchUrlExceptionText =
                    ia.getString(R.string.install_activity__github_rate_limit_exceeded)
                return FAILURE_SHOW_FETCH_URL_EXCEPTION
            } catch (e: NetworkException) {
                ia.viewModel.fetchUrlException = e
                ia.viewModel.fetchUrlExceptionText =
                    ia.getString(R.string.install_activity__temporary_network_issue)
                return FAILURE_SHOW_FETCH_URL_EXCEPTION
            }

            ia.viewModel.updateCheckResult = updateCheckResult
            ia.hide(R.id.fetchUrl)
            ia.show(R.id.fetchedUrlSuccess)
            val finishedText = ia.getString(
                R.string.install_activity__fetched_url_for_download_successfully,
                downloadSource
            )
            ia.setText(R.id.fetchedUrlSuccessTextView, finishedText)
            if (ia.appCache.isAvailable(ia, updateCheckResult.availableResult)) {
                return USE_CACHED_DOWNLOADED_APK
            }

            if (ia.viewModel.fileDownloader?.currentDownloadResult != null) {
                return REUSE_CURRENT_DOWNLOAD
            }
            return START_DOWNLOAD
        }

        @MainThread
        suspend fun startDownload(ia: InstallActivity): State {
            val updateCheckResult = requireNotNull(ia.viewModel.updateCheckResult)
            ia.show(R.id.downloadingFile)
            ia.setText(R.id.downloadingFileUrl, updateCheckResult.downloadUrl)
            ia.appCache.delete(ia)
            val setDownloadingFileText = { text: String ->
                ia.setText(
                    R.id.downloadingFileText,
                    ia.getString(R.string.install_activity__download_app_with_status, text)
                )
            }
            val fileDownloader = FileDownloader()
            fileDownloader.onProgress = { percentage, mb ->
                ia.runOnUiThread {
                    if (percentage != null) {
                        ia.findViewById<ProgressBar>(R.id.downloadingFileProgressBar).progress = percentage
                        setDownloadingFileText("$percentage %, $mb MB")
                    } else {
                        setDownloadingFileText("$mb MB")
                    }
                }
            }
            ia.viewModel.fileDownloader = fileDownloader
            setDownloadingFileText(" %")

            val url = updateCheckResult.availableResult.downloadUrl
            val file = ia.appCache.getFile(ia)

            AppDownloadStatus.foregroundDownloadIsStarted()
            // download coroutine should survive a screen rotation and should live as long as the view model
            val result = withContext(ia.viewModel.viewModelScope.coroutineContext) {
                fileDownloader.downloadFile(url, file)
            }
            AppDownloadStatus.foregroundDownloadIsFinished()
            if (result) {
                return DOWNLOAD_WAS_SUCCESSFUL
            }
            return FAILURE_DOWNLOAD_UNSUCCESSFUL
        }

        @MainThread
        suspend fun reuseCurrentDownload(ia: InstallActivity): State {
            val updateCheckResult = requireNotNull(ia.viewModel.updateCheckResult)
            ia.show(R.id.downloadingFile)
            ia.setText(R.id.downloadingFileUrl, updateCheckResult.downloadUrl)
            val setDownloadingFileText = { text: String ->
                ia.setText(
                    R.id.downloadingFileText,
                    ia.getString(R.string.install_activity__download_app_with_status, text)
                )
            }
            val fileDownloader = requireNotNull(ia.viewModel.fileDownloader)
            fileDownloader.onProgress = { percentage, mb ->
                ia.runOnUiThread {
                    if (percentage != null) {
                        ia.findViewById<ProgressBar>(R.id.downloadingFileProgressBar).progress = percentage
                        setDownloadingFileText("$percentage %")
                    } else {
                        setDownloadingFileText("$mb MB")
                    }
                }
            }

            val success = fileDownloader.currentDownloadResult?.await() ?: false
            AppDownloadStatus.foregroundDownloadIsFinished()
            if (success) {
                return DOWNLOAD_WAS_SUCCESSFUL
            }
            return FAILURE_DOWNLOAD_UNSUCCESSFUL
        }

        @MainThread
        suspend fun downloadWasSuccessful(ia: InstallActivity): State {
            val updateCheckResult = requireNotNull(ia.viewModel.updateCheckResult)

            ia.show(R.id.downloadedFile)
            val app = ia.app
            ia.hide(R.id.downloadingFile)
            ia.setText(R.id.downloadedFileUrl, updateCheckResult.downloadUrl)
            ia.show(R.id.verifyDownloadFingerprint)

            val fingerprint = ia.fingerprintValidator.checkApkFile(ia.appCache.getFile(ia), app)
            ia.fileFingerprint = fingerprint

            if (fingerprint.isValid) {
                return FINGERPRINT_OF_DOWNLOADED_FILE_OK
            }
            ia.appCache.delete(ia)
            return FAILURE_INVALID_FINGERPRINT_OF_DOWNLOADED_FILE
        }

        @MainThread
        suspend fun useCachedDownloadedApk(ia: InstallActivity): State {
            val app = ia.app
            ia.show(R.id.useCachedDownloadedApk)
            ia.setText(R.id.useCachedDownloadedApk__path, ia.appCache.getFile(ia).absolutePath)
            ia.show(R.id.verifyDownloadFingerprint)

            val fingerprint = ia.fingerprintValidator.checkApkFile(ia.appCache.getFile(ia), app)
            ia.fileFingerprint = fingerprint
            return if (fingerprint.isValid) {
                FINGERPRINT_OF_DOWNLOADED_FILE_OK
            } else {
                ia.appCache.delete(ia)
                FAILURE_INVALID_FINGERPRINT_OF_DOWNLOADED_FILE
            }
        }

        @MainThread
        fun fingerprintOfDownloadedFileOk(ia: InstallActivity): State {
            ia.hide(R.id.verifyDownloadFingerprint)
            ia.show(R.id.fingerprintDownloadGood)
            ia.show(R.id.install_activity__retrigger_installation)
            ia.setText(R.id.fingerprintDownloadGoodHash, ia.fileFingerprint.hexString)
            return TRIGGER_INSTALLATION_PROCESS
        }

        @MainThread
        fun triggerInstallationProcess(ia: InstallActivity): State {
            ia.show(R.id.installingApplication)
            ia.appInstaller.install(ia, ia.appCache.getFile(ia))
            return SUCCESS_PAUSE
        }

        @MainThread
        fun userHasInstalledAppSuccessful(ia: InstallActivity): State {
            ia.hide(R.id.installingApplication)
            ia.hide(R.id.install_activity__retrigger_installation)
            ia.show(R.id.installerSuccess)
            return APP_INSTALLATION_HAS_BEEN_REGISTERED
        }

        @MainThread
        suspend fun appInstallationHasBeenRegistered(ia: InstallActivity): State {
            ia.show(R.id.verifyInstalledFingerprint)
            val fingerprint = ia.fingerprintValidator.checkInstalledApp(ia.app)
            ia.appFingerprint = fingerprint
            ia.hide(R.id.verifyInstalledFingerprint)
            return if (fingerprint.isValid) {
                FINGERPRINT_OF_INSTALLED_APP_OK
            } else {
                FAILURE_FINGERPRINT_OF_INSTALLED_APP_INVALID
            }
        }

        @MainThread
        fun fingerprintOfInstalledAppOk(ia: InstallActivity): State {
            val updateCheckResult = requireNotNull(ia.viewModel.updateCheckResult)
            ia.show(R.id.fingerprintInstalledGood)
            ia.setText(R.id.fingerprintInstalledGoodHash, ia.appFingerprint.hexString)
            val available = updateCheckResult.availableResult
            ia.app.detail.appInstallationCallback(ia, available)
            ia.appCache.delete(ia)
            return SUCCESS_STOP
        }

        //===============================================

        @MainThread
        fun failureUnknownSignatureOfInstalledApp(ia: InstallActivity): State {
            ia.show(R.id.unknownSignatureOfInstalledApp)
            return ERROR_STOP
        }

        @MainThread
        fun failureExternalStorageNotAccessible(ia: InstallActivity): State {
            ia.show(R.id.externalStorageNotAccessible)
            ia.setText(
                R.id.externalStorageNotAccessible_state,
                Environment.getExternalStorageState()
            )
            return ERROR_STOP
        }

        @MainThread
        fun failureDownloadUnsuccessful(ia: InstallActivity): State {
            val updateCheckResult = requireNotNull(ia.viewModel.updateCheckResult)
            ia.hide(R.id.downloadingFile)
            ia.show(R.id.downloadFileFailed)
            ia.setText(R.id.downloadFileFailedUrl, updateCheckResult.downloadUrl)
            ia.setText(R.id.downloadFileFailedText, ia.viewModel.fileDownloader?.errorMessage ?: "")
            ia.show(R.id.installerFailed)
            ia.appCache.delete(ia)
            return ERROR_STOP
        }

        @MainThread
        fun failureInvalidFingerprintOfDownloadedFile(ia: InstallActivity): State {
            ia.hide(R.id.verifyDownloadFingerprint)
            ia.show(R.id.fingerprintDownloadBad)
            ia.setText(R.id.fingerprintDownloadBadHashActual, ia.fileFingerprint.hexString)
            ia.setText(R.id.fingerprintDownloadBadHashExpected, ia.app.detail.signatureHash)
            ia.show(R.id.installerFailed)
            ia.appCache.delete(ia)
            return ERROR_STOP
        }

        @MainThread
        fun failureAppInstallation(ia: InstallActivity): State {
            ia.hide(R.id.installingApplication)
            ia.hide(R.id.install_activity__retrigger_installation)
            ia.show(R.id.installerFailed)
            ia.show(R.id.install_activity__delete_cache)
            ia.show(R.id.install_activity__open_cache_folder)
            var error = ia.appInstallationFailedErrorMessage
            if (error != null) {
                if ("INSTALL_FAILED_INTERNAL_ERROR" in error && "Permission Denied" in error) {
                    error += "\n\n${ia.getString(R.string.install_activity__try_disable_miui_optimization)}"
                }
                ia.setText(R.id.installerFailedReason, error)
            }
            return ERROR_STOP
        }

        @MainThread
        fun failureFingerprintOfInstalledAppInvalid(ia: InstallActivity): State {
            ia.show(R.id.fingerprintInstalledBad)
            ia.setText(R.id.fingerprintInstalledBadHashActual, ia.appFingerprint.hexString)
            ia.setText(R.id.fingerprintInstalledBadHashExpected, ia.app.detail.signatureHash)
            ia.appCache.delete(ia)
            return ERROR_STOP
        }

        @MainThread
        fun failureShowFetchUrlException(ia: InstallActivity): State {
            ia.hide(R.id.fetchUrl)
            ia.show(R.id.install_activity__exception)
            val text = ia.viewModel.fetchUrlExceptionText ?: "/"
            ia.setText(R.id.install_activity__exception__text, text)
            val exception = ia.viewModel.fetchUrlException
            if (exception == null) {
                ia.hide(install_activity__exception__show_button)
            } else {
                ia.findViewById<TextView>(install_activity__exception__show_button).setOnClickListener {
                    val description = ia.getString(crash_report__explain_text__install_activity_fetching_url)
                    val intent = CrashReportActivity.createIntent(ia, exception, description)
                    ia.startActivity(intent)
                }
            }
            return ERROR_STOP
        }

        fun createIntent(context: Context, app: App): Intent {
            val intent = Intent(context, InstallActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(EXTRA_APP_NAME, app.name)
            return intent
        }
    }
}