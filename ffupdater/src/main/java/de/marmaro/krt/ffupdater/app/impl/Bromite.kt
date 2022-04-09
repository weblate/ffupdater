package de.marmaro.krt.ffupdater.app.impl

import android.os.Build
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.AvailableVersionResult
import de.marmaro.krt.ffupdater.app.BaseAppWithCachedUpdateCheck
import de.marmaro.krt.ffupdater.app.impl.fetch.ApiConsumer
import de.marmaro.krt.ffupdater.app.impl.fetch.github.GithubConsumer
import de.marmaro.krt.ffupdater.device.ABI

/**
 * https://github.com/bromite/bromite/releases
 * https://api.github.com/repos/bromite/bromite/releases
 * https://www.apkmirror.com/apk/bromite/bromite/
 */
class Bromite(
    private val failIfValidReleaseHasNoValidAsset: Boolean = false,
    private val apiConsumer: ApiConsumer,
    private val deviceAbis: List<ABI>,
) : BaseAppWithCachedUpdateCheck() {
    override val packageName = "org.bromite.bromite"
    override val displayTitle = R.string.bromite__title
    override val displayDescription = R.string.bromite__description
    override val displayWarning = R.string.bromite__warning
    override val displayDownloadSource = R.string.github
    override val displayIcon = R.mipmap.ic_logo_bromite
    override val minApiLevel = Build.VERSION_CODES.LOLLIPOP
    override val supportedAbis = listOf(ABI.ARM64_V8A, ABI.ARMEABI_V7A, ABI.X86, ABI.X86_64)

    @Suppress("SpellCheckingInspection")
    override val signatureHash = "e1ee5cd076d7b0dc84cb2b45fb78b86df2eb39a3b6c56ba3dc292a5e0c3b9504"

    override suspend fun updateCheckWithoutCaching(): AvailableVersionResult {
        val filteredAbis = deviceAbis.filter { it in supportedAbis }
        val fileName = when (filteredAbis.firstOrNull()) {
            ABI.ARMEABI_V7A -> "arm_ChromePublic.apk"
            ABI.ARM64_V8A -> "arm64_ChromePublic.apk"
            ABI.X86 -> "x86_ChromePublic.apk"
            ABI.X86_64 -> "x64_ChromePublic.apk"
            else -> throw IllegalArgumentException("ABI '${filteredAbis.firstOrNull()}' is not supported")
        }
        val githubConsumer = GithubConsumer(
            repoOwner = "bromite",
            repoName = "bromite",
            resultsPerPage = 5,
            isValidRelease = { release -> !release.isPreRelease },
            failIfValidReleaseHasNoValidAsset = failIfValidReleaseHasNoValidAsset,
            isCorrectAsset = { asset -> asset.name == fileName },
            apiConsumer = apiConsumer,
        )
        val result = githubConsumer.updateCheck()
        // tag name can be "90.0.4430.59"
        return AvailableVersionResult(
            downloadUrl = result.url,
            version = result.tagName,
            publishDate = result.releaseDate,
            fileSizeBytes = result.fileSizeBytes,
            fileHash = null
        )
    }
}