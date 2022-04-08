package de.marmaro.krt.ffupdater.app.impl.fetch.github

import androidx.annotation.MainThread
import com.google.gson.annotations.SerializedName
import de.marmaro.krt.ffupdater.app.impl.exceptions.InvalidApiResponseException
import de.marmaro.krt.ffupdater.app.impl.fetch.ApiConsumer
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import java.util.*
import java.util.function.Predicate

class GithubConsumer(
    repoOwner: String,
    repoName: String,
    private val resultsPerPage: Int,
    private val isValidRelease: Predicate<Release>,
    private val isCorrectAsset: Predicate<Asset>,
    private val failIfValidReleaseHasNoValidAsset: Boolean = false,
    // false -> contact "$url/latest" and then "$url?per_page=..&page=.."
    // true -> contact only "$url?per_page=..&page=.."
    // set it to true if it is unlikely that the latest release is a valid release
    private val onlyRequestReleasesInBulk: Boolean = false,
    private val apiConsumer: ApiConsumer,
) {
    private val url = "https://api.github.com/repos/$repoOwner/$repoName/releases"

    init {
        check(resultsPerPage > 0)
    }

    @MainThread
    suspend fun updateCheck(): Result {
        val start = if (onlyRequestReleasesInBulk) 2 else 1
        for (tries in start..5) {
            val releases = requestReleases(tries)
            val validReleases = releases.filter { isReleaseValid(it) }

            if (failIfValidReleaseHasNoValidAsset && validReleases.isNotEmpty() &&
                !isAnyAssetValid(validReleases[0])
            ) {
                throw InvalidApiResponseException("first valid release has no valid asset")
            }

            val firstReleaseWithValidAsset = validReleases.firstOrNull { isAnyAssetValid(it) } ?: continue
            val firstValidAsset = firstReleaseWithValidAsset.assets.first { isAssetValid(it) }
            return Result(
                tagName = firstReleaseWithValidAsset.tagName,
                url = firstValidAsset.downloadUrl,
                fileSizeBytes = firstValidAsset.fileSizeBytes,
                releaseDate = ZonedDateTime.parse(firstReleaseWithValidAsset.publishedAt, ISO_ZONED_DATE_TIME)
            )
        }
        throw InvalidApiResponseException("can't find release after all tries - abort")
    }

    @MainThread
    suspend fun requestReleases(tries: Int): Array<Release> {
        when (tries) {
            1 -> {
                val release = apiConsumer.consumeNetworkResource("$url/latest", Release::class)
                return arrayOf(release)
            }
            2, 3, 4, 5 -> {
                val url = "$url?per_page=$resultsPerPage&page=${tries - 1}"
                return apiConsumer.consumeNetworkResource(url, Array<Release>::class)
            }
        }
        throw InvalidApiResponseException("can't find release after $tries tries - abort")
    }

    private fun isReleaseValid(release: Release): Boolean {
        return isValidRelease.test(release)
    }

    private fun isAssetValid(asset: Asset): Boolean {
        return isCorrectAsset.test(asset)
    }

    private fun isAnyAssetValid(release: Release): Boolean {
        return release.assets.any { isAssetValid(it) }
    }

    data class Release(
        @SerializedName("tag_name")
        val tagName: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("prerelease")
        val isPreRelease: Boolean,
        @SerializedName("assets")
        val assets: List<Asset>,
        @SerializedName("published_at")
        val publishedAt: String,
    )

    data class Asset(
        @SerializedName("name")
        val name: String,
        @SerializedName("browser_download_url")
        val downloadUrl: String,
        @SerializedName("size")
        val fileSizeBytes: Long,
    )

    data class Result(
        val tagName: String,
        val url: String,
        val fileSizeBytes: Long,
        val releaseDate: ZonedDateTime,
    )
}