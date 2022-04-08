package de.marmaro.krt.ffupdater.app.impl

import android.os.Build
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.AvailableVersionResult
import de.marmaro.krt.ffupdater.app.BaseAppWithCachedUpdateCheck
import de.marmaro.krt.ffupdater.app.impl.fetch.ApiConsumer
import de.marmaro.krt.ffupdater.app.impl.fetch.github.GithubConsumer
import de.marmaro.krt.ffupdater.device.ABI

/**
 * https://api.github.com/repos/mozilla-lockwise/lockwise-android/releases
 * https://www.apkmirror.com/apk/mozilla/firefox-lockwise/
 */
class Lockwise(
    private val failIfValidReleaseHasNoValidAsset: Boolean = false,
    private val apiConsumer: ApiConsumer,
) : BaseAppWithCachedUpdateCheck() {
    override val packageName = "mozilla.lockbox"
    override val displayTitle = R.string.lockwise__title
    override val displayDescription = R.string.lockwise__description
    override val displayWarning: Int? = null
    override val displayDownloadSource = R.string.github
    override val displayIcon = R.mipmap.ic_logo_lockwise
    override val minApiLevel = Build.VERSION_CODES.N
    override val supportedAbis = listOf(
        ABI.ARM64_V8A, ABI.ARMEABI_V7A, ABI.ARMEABI, ABI.X86_64,
        ABI.X86, ABI.MIPS, ABI.MIPS64
    )

    @Suppress("SpellCheckingInspection")
    override val signatureHash = "64d26b507078deba2fee42d6bd0bfad41d39ffc4e791f281028e5e73d3c8d2f2"

    override suspend fun updateCheckWithoutCaching(): AvailableVersionResult {
        val githubConsumer = GithubConsumer(
            repoOwner = "mozilla-lockwise",
            repoName = "lockwise-android",
            resultsPerPage = 5,
            isValidRelease = { release -> !release.isPreRelease },
            isCorrectAsset = { asset -> asset.name.endsWith(".apk") },
            failIfValidReleaseHasNoValidAsset = failIfValidReleaseHasNoValidAsset,
            apiConsumer = apiConsumer,
        )
        val result = githubConsumer.updateCheck()

        val extractVersion = {
            // tag_name can be: "release-v4.0.3", "release-v4.0.0-RC-2"
            val regexMatch = Regex("""^release-v((\d)+(\.\d+)*)""")
                .find(result.tagName)
            checkNotNull(regexMatch) {
                "Fail to extract the version with regex from string: \"${result.tagName}\""
            }
            val matchGroup = regexMatch.groups[1]
            checkNotNull(matchGroup) {
                "Fail to extract the version value from regex match: \"${regexMatch.value}\""
            }
            matchGroup.value
        }

        return AvailableVersionResult(
            downloadUrl = result.url,
            version = extractVersion(),
            publishDate = result.releaseDate,
            fileSizeBytes = result.fileSizeBytes,
            fileHash = null
        )
    }
}