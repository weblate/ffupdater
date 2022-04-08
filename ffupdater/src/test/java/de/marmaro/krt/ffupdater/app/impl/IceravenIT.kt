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
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.FileReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class IceravenIT {
    @MockK
    lateinit var context: Context

    @MockK
    lateinit var packageManager: PackageManager

    @MockK
    lateinit var apiConsumer: ApiConsumer

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { context.packageManager } returns packageManager
        every { context.getString(R.string.available_version, any()) } returns "/"
    }

    companion object {
        const val API_URL = "https://api.github.com/repos/fork-maintainers/iceraven-browser/" +
                "releases/latest"
        const val DOWNLOAD_URL = "https://github.com/fork-maintainers/iceraven-browser/releases/" +
                "download/iceraven-1.6.0"
    }

    private fun createSut(deviceAbi: ABI): Iceraven {
        return Iceraven(apiConsumer = apiConsumer, deviceAbis = listOf(deviceAbi))
    }

    private fun makeReleaseJsonObjectAvailable() {
        val path = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/Iceraven/latest.json"
        coEvery {
            apiConsumer.consumeNetworkResource(API_URL, GithubConsumer.Release::class)
        } returns Gson().fromJson(FileReader(path), GithubConsumer.Release::class.java)
    }

    @Test
    fun updateCheck_latestRelease_checkDownloadUrlForABI() {
        makeReleaseJsonObjectAvailable()
        val packageInfo = PackageInfo()
        packageInfo.versionName = "1.19.92"
        every {
            packageManager.getPackageInfo(App.ICERAVEN.detail.packageName, any())
        } returns packageInfo

        runBlocking {
            assertEquals(
                "$DOWNLOAD_URL/iceraven-1.6.0-browser-armeabi-v7a-forkRelease.apk",
                createSut(ABI.ARMEABI_V7A).updateCheck(context).downloadUrl
            )
        }

        runBlocking {
            assertEquals(
                "$DOWNLOAD_URL/iceraven-1.6.0-browser-arm64-v8a-forkRelease.apk",
                createSut(ABI.ARM64_V8A).updateCheck(context).downloadUrl
            )
        }

        runBlocking {
            assertEquals(
                "$DOWNLOAD_URL/iceraven-1.6.0-browser-x86-forkRelease.apk",
                createSut(ABI.X86).updateCheck(context).downloadUrl
            )
        }

        runBlocking {
            assertEquals(
                "$DOWNLOAD_URL/iceraven-1.6.0-browser-x86_64-forkRelease.apk",
                createSut(ABI.X86_64).updateCheck(context).downloadUrl
            )
        }
    }

    @Test
    fun updateCheck_latestRelease_updateCheck() {
        makeReleaseJsonObjectAvailable()
        val packageInfo = PackageInfo()
        every {
            packageManager.getPackageInfo(App.ICERAVEN.detail.packageName, any())
        } returns packageInfo

        // installed app is up-to-date
        runBlocking {
            packageInfo.versionName = "iceraven-1.6.0"
            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
            assertFalse(actual.isUpdateAvailable)
            assertEquals("1.6.0", actual.version)
            assertEquals(66150140L, actual.fileSizeBytes)
            assertEquals(
                ZonedDateTime.parse("2021-02-07T00:37:13Z", DateTimeFormatter.ISO_ZONED_DATE_TIME),
                actual.publishDate
            )
        }

        // installed app is old
        runBlocking {
            packageInfo.versionName = "iceraven-1.5.0"
            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
            assertTrue(actual.isUpdateAvailable)
            assertEquals("1.6.0", actual.version)
            assertEquals(66150140L, actual.fileSizeBytes)
            assertEquals(
                ZonedDateTime.parse("2021-02-07T00:37:13Z", DateTimeFormatter.ISO_ZONED_DATE_TIME),
                actual.publishDate
            )
        }
    }
}