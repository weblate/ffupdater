package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.impl.fetch.ApiConsumer
import de.marmaro.krt.ffupdater.device.ABI
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

class FirefoxBetaIT {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var packageManager: PackageManager
    private var packageInfo = PackageInfo()

    @MockK
    lateinit var apiConsumer: ApiConsumer

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { context.packageManager } returns packageManager
        packageInfo.versionName = ""
        every {
            packageManager.getPackageInfo(App.FIREFOX_BETA.detail.packageName, any())
        } returns packageInfo
        every { context.packageName } returns "de.marmaro.krt.ffupdater"
        every { context.getString(R.string.available_version, any()) } returns "/"
    }

    companion object {
        const val BASE_URL = "https://firefox-ci-tc.services.mozilla.com/api/index/v1/task/" +
                "mobile.v2.fenix.beta.latest"
    }

    private fun createSut(deviceAbi: ABI): FirefoxBeta {
        return FirefoxBeta(apiConsumer = apiConsumer, deviceAbis = listOf(deviceAbi))
    }

    private fun makeChainOfTrustTextAvailableUnderUrl(url: String) {
        val path = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/FirefoxBeta/" +
                "chain_of_trust.log"
        coEvery {
            apiConsumer.consumeNetworkResource(url, String::class)
        } returns File(path).readText()
    }

    @Test
    fun updateCheck_armeabiv7a_upToDate() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.armeabi-v7a/artifacts/public/logs/chain_of_trust.log")
        packageInfo.versionName = "91.0.0-beta.3"

        val actual = runBlocking { createSut(ABI.ARMEABI_V7A).updateCheck(context) }

        assertFalse(actual.isUpdateAvailable)
        assertEquals("91.0.0-beta.3", actual.version)
        val expectedUrl = "$BASE_URL.armeabi-v7a/artifacts/public/build/armeabi-v7a/target.apk"
        assertEquals(expectedUrl, actual.downloadUrl)
        val expectedDate = ZonedDateTime.parse("2021-07-22T13:29:07Z", ISO_ZONED_DATE_TIME)
        assertEquals(expectedDate, actual.publishDate)
    }

    @Test
    fun updateCheck_armeabiv7a_updateAvailable() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.armeabi-v7a/artifacts/public/logs/chain_of_trust.log")
        packageInfo.versionName = "86.0.0-beta.3"

        val actual = runBlocking { createSut(ABI.ARMEABI_V7A).updateCheck(context) }

        assertTrue(actual.isUpdateAvailable)
        assertEquals("91.0.0-beta.3", actual.version)
        val expectedUrl = "$BASE_URL.armeabi-v7a/artifacts/public/build/armeabi-v7a/target.apk"
        assertEquals(expectedUrl, actual.downloadUrl)
        val expectedDate = ZonedDateTime.parse("2021-07-22T13:29:07Z", ISO_ZONED_DATE_TIME)
        assertEquals(expectedDate, actual.publishDate)
    }

    @Test
    fun updateCheck_arm64v8a_upToDate() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.arm64-v8a/artifacts/public/logs/chain_of_trust.log")
        packageInfo.versionName = "91.0.0-beta.3"

        val actual = runBlocking { createSut(ABI.ARM64_V8A).updateCheck(context) }

        assertFalse(actual.isUpdateAvailable)
        assertEquals("91.0.0-beta.3", actual.version)
        val expectedUrl = "$BASE_URL.arm64-v8a/artifacts/public/build/arm64-v8a/target.apk"
        assertEquals(expectedUrl, actual.downloadUrl)
        val expectedDate = ZonedDateTime.parse("2021-07-22T13:29:07Z", ISO_ZONED_DATE_TIME)
        assertEquals(expectedDate, actual.publishDate)
    }

    @Test
    fun updateCheck_arm64v8a_updateAvailable() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.arm64-v8a/artifacts/public/logs/chain_of_trust.log")
        packageInfo.versionName = "86.0.0-beta.3"

        val actual = runBlocking { createSut(ABI.ARM64_V8A).updateCheck(context) }

        assertTrue(actual.isUpdateAvailable)
        assertEquals("91.0.0-beta.3", actual.version)
        val expectedUrl = "$BASE_URL.arm64-v8a/artifacts/public/build/arm64-v8a/target.apk"
        assertEquals(expectedUrl, actual.downloadUrl)
        val expectedDate = ZonedDateTime.parse("2021-07-22T13:29:07Z", ISO_ZONED_DATE_TIME)
        assertEquals(expectedDate, actual.publishDate)
    }

    @Test
    fun updateCheck_x86_upToDate() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.x86/artifacts/public/logs/chain_of_trust.log")
        packageInfo.versionName = "91.0.0-beta.3"

        val actual = runBlocking { createSut(ABI.X86).updateCheck(context) }

        assertFalse(actual.isUpdateAvailable)
        assertEquals("91.0.0-beta.3", actual.version)
        val expectedUrl = "$BASE_URL.x86/artifacts/public/build/x86/target.apk"
        assertEquals(expectedUrl, actual.downloadUrl)
        val expectedDate = ZonedDateTime.parse("2021-07-22T13:29:07Z", ISO_ZONED_DATE_TIME)
        assertEquals(expectedDate, actual.publishDate)
    }

    @Test
    fun updateCheck_x86_updateAvailable() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.x86/artifacts/public/logs/chain_of_trust.log")
        packageInfo.versionName = "86.0.0-beta.3"

        val actual = runBlocking { createSut(ABI.X86).updateCheck(context) }

        assertTrue(actual.isUpdateAvailable)
        assertEquals("91.0.0-beta.3", actual.version)
        val expectedUrl = "$BASE_URL.x86/artifacts/public/build/x86/target.apk"
        assertEquals(expectedUrl, actual.downloadUrl)
        val expectedDate = ZonedDateTime.parse("2021-07-22T13:29:07Z", ISO_ZONED_DATE_TIME)
        assertEquals(expectedDate, actual.publishDate)
    }

    @Test
    fun updateCheck_x8664_upToDate() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.x86_64/artifacts/public/logs/chain_of_trust.log")
        packageInfo.versionName = "91.0.0-beta.3"

        val actual = runBlocking { createSut(ABI.X86_64).updateCheck(context) }

        assertFalse(actual.isUpdateAvailable)
        assertEquals("91.0.0-beta.3", actual.version)
        val expectedUrl = "$BASE_URL.x86_64/artifacts/public/build/x86_64/target.apk"
        assertEquals(expectedUrl, actual.downloadUrl)
        val expectedDate = ZonedDateTime.parse("2021-07-22T13:29:07Z", ISO_ZONED_DATE_TIME)
        assertEquals(expectedDate, actual.publishDate)
    }

    @Test
    fun updateCheck_x8664_updateAvailable() {
        makeChainOfTrustTextAvailableUnderUrl("$BASE_URL.x86_64/artifacts/public/logs/chain_of_trust.log")
        packageInfo.versionName = "86.0.0-beta.3"

        val actual = runBlocking { createSut(ABI.X86_64).updateCheck(context) }

        assertTrue(actual.isUpdateAvailable)
        assertEquals("91.0.0-beta.3", actual.version)
        val expectedUrl = "$BASE_URL.x86_64/artifacts/public/build/x86_64/target.apk"
        assertEquals(expectedUrl, actual.downloadUrl)
        val expectedDate = ZonedDateTime.parse("2021-07-22T13:29:07Z", ISO_ZONED_DATE_TIME)
        assertEquals(expectedDate, actual.publishDate)
    }
}