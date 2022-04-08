package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.google.gson.Gson
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.impl.fetch.ApiConsumer
import de.marmaro.krt.ffupdater.app.impl.fetch.github.GithubConsumer
import de.marmaro.krt.ffupdater.device.ABI
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.io.FileReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(MockKExtension::class)
class LockwiseIT {
    @MockK
    lateinit var context: Context

    @MockK
    lateinit var packageManager: PackageManager

    @MockK
    lateinit var apiConsumer: ApiConsumer

    @BeforeEach
    fun setUp() {
        every { context.packageManager } returns packageManager
        every { context.getString(R.string.available_version, any()) } returns "/"
    }

    companion object {
        const val API_URL = "https://api.github.com/repos/mozilla-lockwise/lockwise-android/" +
                "releases"
        const val DOWNLOAD_URL = "https://github.com/mozilla-lockwise/lockwise-android/releases/" +
                "download"
    }

    private fun createSut(): Lockwise {
        return Lockwise(apiConsumer = apiConsumer)
    }

    private fun makeReleaseJsonObjectAvailableUnderUrl(fileName: String, url: String) {
        val path = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/Lockwise/$fileName"
        coEvery {
            apiConsumer.consumeNetworkResource(url, GithubConsumer.Release::class)
        } returns Gson().fromJson(FileReader(path), GithubConsumer.Release::class.java)
    }

    private fun makeReleaseJsonArrayAvailableUnderUrl(fileName: String, url: String) {
        val path = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/Lockwise//$fileName"
        coEvery {
            apiConsumer.consumeNetworkResource(url, Array<GithubConsumer.Release>::class)
        } returns Gson().fromJson(FileReader(path), Array<GithubConsumer.Release>::class.java)
    }

    @Test
    fun updateCheck_latestRelease_checkDownloadUrlForABI() {
        makeReleaseJsonObjectAvailableUnderUrl("latest.json", "$API_URL/latest")
        val packageInfo = PackageInfo()
        packageInfo.versionName = "4.0.3"
        every {
            packageManager.getPackageInfo(App.LOCKWISE.detail.packageName, any())
        } returns packageInfo

        for (abi in ABI.values()) {
            val actual = runBlocking {
                createSut().updateCheck(context).downloadUrl
            }
            val expected = "$DOWNLOAD_URL/release-v4.0.3/lockbox-app-release-6584-signed.apk"
            assertEquals(expected, actual)
        }
    }

    @Test
    fun updateCheck_latestRelease_updateCheck() {
        makeReleaseJsonObjectAvailableUnderUrl("latest.json", "$API_URL/latest")
        val packageInfo = PackageInfo()
        every {
            packageManager.getPackageInfo(App.LOCKWISE.detail.packageName, any())
        } returns packageInfo

        // installed app is up-to-date
        runBlocking {
            packageInfo.versionName = "4.0.3"
            val actual = createSut().updateCheck(context)
            assertFalse(actual.isUpdateAvailable)
            assertEquals("4.0.3", actual.version)
            assertEquals(37188004L, actual.fileSizeBytes)
            assertEquals(
                ZonedDateTime.parse("2020-12-10T18:40:16Z", DateTimeFormatter.ISO_ZONED_DATE_TIME),
                actual.publishDate
            )
        }

        // installed app is old
        runBlocking {
            packageInfo.versionName = "4.0.0"
            val actual = createSut().updateCheck(context)
            assertTrue(actual.isUpdateAvailable)
            assertEquals("4.0.3", actual.version)
            assertEquals(37188004L, actual.fileSizeBytes)
            assertEquals(
                ZonedDateTime.parse("2020-12-10T18:40:16Z", DateTimeFormatter.ISO_ZONED_DATE_TIME),
                actual.publishDate
            )
        }
    }

    @Test
    fun updateCheck_2releases_checkDownloadUrlForABI() {
        makeReleaseJsonObjectAvailableUnderUrl("2releases_latest.json", "$API_URL/latest")
        makeReleaseJsonArrayAvailableUnderUrl("2releases_page1.json", "$API_URL?per_page=5&page=1")
        val packageInfo = PackageInfo()
        every {
            packageManager.getPackageInfo(App.LOCKWISE.detail.packageName, any())
        } returns packageInfo

        for (abi in ABI.values()) {
            val actual = runBlocking {
                createSut().updateCheck(context).downloadUrl
            }
            val expected = "$DOWNLOAD_URL/release-v3.3.0-RC-2/lockbox-app-release-5784-signed.apk"
            assertEquals(expected, actual)
        }
    }

    @Test
    fun updateCheck_2releases_updateCheck() {
        makeReleaseJsonObjectAvailableUnderUrl("2releases_latest.json", "$API_URL/latest")
        makeReleaseJsonArrayAvailableUnderUrl("2releases_page1.json", "$API_URL?per_page=5&page=1")
        val packageInfo = PackageInfo()
        every {
            packageManager.getPackageInfo(App.LOCKWISE.detail.packageName, any())
        } returns packageInfo

        // installed app is up-to-date
        runBlocking {
            packageInfo.versionName = "3.3.0"
            val actual = createSut().updateCheck(context)
            assertFalse(actual.isUpdateAvailable)
            assertEquals("3.3.0", actual.version)
            assertEquals(19367045L, actual.fileSizeBytes)
            assertEquals(
                ZonedDateTime.parse("2019-12-13T19:34:17Z", DateTimeFormatter.ISO_ZONED_DATE_TIME),
                actual.publishDate
            )
        }

        // installed app is old
        runBlocking {
            packageInfo.versionName = "3.2.0"
            val actual = createSut().updateCheck(context)
            assertTrue(actual.isUpdateAvailable)
            assertEquals("3.3.0", actual.version)
            assertEquals(19367045L, actual.fileSizeBytes)
            assertEquals(
                ZonedDateTime.parse("2019-12-13T19:34:17Z", DateTimeFormatter.ISO_ZONED_DATE_TIME),
                actual.publishDate
            )
        }
    }
}