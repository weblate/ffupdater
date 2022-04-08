package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.impl.fetch.ApiConsumer
import de.marmaro.krt.ffupdater.device.ABI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

@ExtendWith(MockKExtension::class)
class FirefoxReleaseIT {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var packageManager: PackageManager
    private var packageInfo = PackageInfo()

    @MockK
    lateinit var apiConsumer: ApiConsumer

    @BeforeEach
    fun setUp() {
        every { context.packageManager } returns packageManager
        packageInfo.versionName = ""
        every {
            packageManager.getPackageInfo(App.FIREFOX_RELEASE.detail.packageName, any())
        } returns packageInfo
        every { context.getString(R.string.available_version, any()) } returns "/"
        every { context.packageName } returns "de.marmaro.krt.ffupdater"
    }

    companion object {
        const val BASE_URL = "https://firefox-ci-tc.services.mozilla.com/api/index/v1/task/" +
                "mobile.v2.fenix.release.latest"
    }

    private fun createSut(deviceAbi: ABI): FirefoxRelease {
        return FirefoxRelease(apiConsumer = apiConsumer, deviceAbis = listOf(deviceAbi))
    }

    private fun makeChainOfTrustTextAvailableUnderUrl(url: String) {
        val path = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/FirefoxRelease/" +
                "chain-of-trust.log"
        coEvery {
            apiConsumer.consumeNetworkResource(url, String::class)
        } returns File(path).readText()
    }

    @Test
    fun updateCheck_armeabiv7a() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.armeabi-v7a/artifacts/public/logs/chain_of_trust.log")
        val expectedUrl = "$BASE_URL.armeabi-v7a/artifacts/public/build/armeabi-v7a/target.apk"
        val expectedTime = ZonedDateTime.parse("2021-07-19T15:07:50.886Z", ISO_ZONED_DATE_TIME)

        runBlocking {
            packageInfo.versionName = "90.1.2"
            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
            assertFalse(actual.isUpdateAvailable)
            assertEquals("90.1.2", actual.version)
            assertEquals(expectedUrl, actual.downloadUrl)
            assertEquals(expectedTime, actual.publishDate)
        }

        runBlocking {
            packageInfo.versionName = "85.1.2"
            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
            assertTrue(actual.isUpdateAvailable)
            assertEquals("90.1.2", actual.version)
            assertEquals(expectedUrl, actual.downloadUrl)
            assertEquals(expectedTime, actual.publishDate)
        }
    }

    @Test
    fun updateCheck_arm64v8a() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.arm64-v8a/artifacts/public/logs/chain_of_trust.log")
        val expectedUrl = "$BASE_URL.arm64-v8a/artifacts/public/build/arm64-v8a/target.apk"
        val expectedTime = ZonedDateTime.parse("2021-07-19T15:07:50.886Z", ISO_ZONED_DATE_TIME)

        runBlocking {
            packageInfo.versionName = "90.1.2"
            val actual = createSut(ABI.ARM64_V8A).updateCheck(context)
            assertFalse(actual.isUpdateAvailable)
            assertEquals("90.1.2", actual.version)
            assertEquals(expectedUrl, actual.downloadUrl)
            assertEquals(expectedTime, actual.publishDate)
        }

        runBlocking {
            packageInfo.versionName = "85.1.2"
            val actual = createSut(ABI.ARM64_V8A).updateCheck(context)
            assertTrue(actual.isUpdateAvailable)
            assertEquals("90.1.2", actual.version)
            assertEquals(expectedUrl, actual.downloadUrl)
            assertEquals(expectedTime, actual.publishDate)
        }
    }

    @Test
    fun updateCheck_x86() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.x86/artifacts/public/logs/chain_of_trust.log")
        val expectedUrl = "$BASE_URL.x86/artifacts/public/build/x86/target.apk"
        val expectedTime = ZonedDateTime.parse("2021-07-19T15:07:50.886Z", ISO_ZONED_DATE_TIME)

        runBlocking {
            packageInfo.versionName = "90.1.2"
            val actual = createSut(ABI.X86).updateCheck(context)
            assertFalse(actual.isUpdateAvailable)
            assertEquals("90.1.2", actual.version)
            assertEquals(expectedUrl, actual.downloadUrl)
            assertEquals(expectedTime, actual.publishDate)
        }

        runBlocking {
            packageInfo.versionName = "85.1.2"
            val actual = createSut(ABI.X86).updateCheck(context)
            assertTrue(actual.isUpdateAvailable)
            assertEquals("90.1.2", actual.version)
            assertEquals(expectedUrl, actual.downloadUrl)
            assertEquals(expectedTime, actual.publishDate)
        }
    }

    @Test
    fun updateCheck_x8664() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.x86_64/artifacts/public/logs/chain_of_trust.log")
        val expectedUrl = "$BASE_URL.x86_64/artifacts/public/build/x86_64/target.apk"
        val expectedTime = ZonedDateTime.parse("2021-07-19T15:07:50.886Z", ISO_ZONED_DATE_TIME)

        runBlocking {
            packageInfo.versionName = "90.1.2"
            val actual = createSut(ABI.X86_64).updateCheck(context)
            assertFalse(actual.isUpdateAvailable)
            assertEquals("90.1.2", actual.version)
            assertEquals(expectedUrl, actual.downloadUrl)
            assertEquals(expectedTime, actual.publishDate)
        }

        runBlocking {
            packageInfo.versionName = "85.1.2"
            val actual = createSut(ABI.X86_64).updateCheck(context)
            assertTrue(actual.isUpdateAvailable)
            assertEquals("90.1.2", actual.version)
            assertEquals(expectedUrl, actual.downloadUrl)
            assertEquals(expectedTime, actual.publishDate)
        }
    }
}