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
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

class BromiteIT {
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
        coEvery {
            apiConsumer.consumeNetworkResource(
                "https://api.github.com/repos/bromite/bromite/releases/latest",
                GithubConsumer.Release::class
            )
        } returns Gson().fromJson(
            FileReader("$FOLDER_PATH/latest_contains_release_version.json"),
            GithubConsumer.Release::class.java
        )
    }

    companion object {
        const val DOWNLOAD_URL = "https://github.com/bromite/bromite/releases/download/100.0.4896.57"
        const val FOLDER_PATH = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/Bromite"
    }

    private fun createSut(deviceAbi: ABI): Bromite {
        return Bromite(apiConsumer = apiConsumer, deviceAbis = listOf(deviceAbi))
    }

    @Test
    suspend fun updateCheck_latestRelease_checkDownloadUrlForABI() {
        val packageInfo = PackageInfo()
        packageInfo.versionName = "90.0.4430.59"
        every {
            packageManager.getPackageInfo(
                App.BROMITE.detail.packageName,
                0
            )
        } returns packageInfo

        assertEquals(
            "$DOWNLOAD_URL/arm_ChromePublic.apk",
            createSut(ABI.ARMEABI_V7A).updateCheck(context).downloadUrl
        )

        assertEquals(
            "$DOWNLOAD_URL/arm64_ChromePublic.apk",
            createSut(ABI.ARM64_V8A).updateCheck(context).downloadUrl
        )

        assertEquals(
            "$DOWNLOAD_URL/x86_ChromePublic.apk",
            createSut(ABI.X86).updateCheck(context).downloadUrl
        )

        assertEquals(
            "$DOWNLOAD_URL/x64_ChromePublic.apk",
            createSut(ABI.X86_64).updateCheck(context).downloadUrl
        )
    }

    @Test
    fun updateCheck_latestRelease_updateCheck() {
        val packageInfo = PackageInfo()
        every {
            packageManager.getPackageInfo(
                App.BROMITE.detail.packageName,
                0
            )
        } returns packageInfo

        // installed app is up-to-date
        runBlocking {
            packageInfo.versionName = "100.0.4896.57"
            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
            assertFalse(actual.isUpdateAvailable)
            assertEquals("100.0.4896.57", actual.version)
            assertEquals(118003761L, actual.fileSizeBytes)
            assertEquals(
                ZonedDateTime.parse("2022-03-29T21:36:18Z", ISO_ZONED_DATE_TIME),
                actual.publishDate
            )
        }

        // installed app is old
        runBlocking {
            packageInfo.versionName = "89.0.4389.117"
            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
            assertTrue(actual.isUpdateAvailable)
            assertEquals("100.0.4896.57", actual.version)
            assertEquals(118003761L, actual.fileSizeBytes)
            assertEquals(
                ZonedDateTime.parse("2022-03-29T21:36:18Z", ISO_ZONED_DATE_TIME),
                actual.publishDate
            )
        }
    }
}