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
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.io.FileReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

@ExtendWith(MockKExtension::class)
class FirefoxFocusIT {
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
            packageManager.getPackageInfo(App.FIREFOX_FOCUS.detail.packageName, any())
        } returns packageInfo
        every { context.getString(R.string.available_version, any()) } returns "/"
        every { context.packageName } returns "de.marmaro.krt.ffupdater"
    }

    companion object {
        const val DOWNLOAD_URL = "https://github.com/mozilla-mobile/focus-android/releases/download"
    }

    private fun createSut(deviceAbi: ABI): FirefoxFocus {
        return FirefoxFocus(apiConsumer = apiConsumer, deviceAbis = listOf(deviceAbi))
    }

    private fun makeReleaseJsonObjectAvailable() {
        val url = "https://api.github.com/repos/mozilla-mobile/focus-android/releases/latest"
        val path = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/FirefoxFocus/latest.json"
        coEvery {
            apiConsumer.consumeNetworkResource(url, GithubConsumer.Release::class)
        } returns Gson().fromJson(FileReader(path), GithubConsumer.Release::class.java)
    }

    @Test
    fun `check url, time and version (ARM64_V8A)`() {
        makeReleaseJsonObjectAvailable()
        val actual = runBlocking { createSut(ABI.ARM64_V8A).updateCheck(context) }
        assertEquals("98.1.0", actual.version)
        assertEquals("$DOWNLOAD_URL/v98.1.0/focus-98.1.0-arm64-v8a.apk", actual.downloadUrl)
        assertEquals(ZonedDateTime.parse("2022-03-01T14:16:00Z", ISO_ZONED_DATE_TIME), actual.publishDate)
    }

    @Test
    fun `check url, time and version (ARMEABI_V7A)`() {
        makeReleaseJsonObjectAvailable()
        val actual = runBlocking { createSut(ABI.ARMEABI_V7A).updateCheck(context) }
        assertEquals("98.1.0", actual.version)
        assertEquals("$DOWNLOAD_URL/v98.1.0/focus-98.1.0-armeabi-v7a.apk", actual.downloadUrl)
        assertEquals(ZonedDateTime.parse("2022-03-01T14:16:00Z", ISO_ZONED_DATE_TIME), actual.publishDate)
    }

    @Test
    fun `check url, time and version (X86)`() {
        makeReleaseJsonObjectAvailable()
        val actual = runBlocking { createSut(ABI.X86).updateCheck(context) }
        assertEquals("98.1.0", actual.version)
        assertEquals("$DOWNLOAD_URL/v98.1.0/focus-98.1.0-x86.apk", actual.downloadUrl)
        assertEquals(ZonedDateTime.parse("2022-03-01T14:16:00Z", ISO_ZONED_DATE_TIME), actual.publishDate)
    }

    @Test
    fun `check url, time and version (X86_64)`() {
        makeReleaseJsonObjectAvailable()
        val actual = runBlocking { createSut(ABI.X86_64).updateCheck(context) }
        assertEquals("98.1.0", actual.version)
        assertEquals("$DOWNLOAD_URL/v98.1.0/focus-98.1.0-x86_64.apk", actual.downloadUrl)
        assertEquals(ZonedDateTime.parse("2022-03-01T14:16:00Z", ISO_ZONED_DATE_TIME), actual.publishDate)
    }

    @Test
    fun `negative update check for up-to-date app (ARM64_V8A)`() {
        makeReleaseJsonObjectAvailable()
        packageInfo.versionName = "98.1.0"
        val actual = runBlocking { createSut(ABI.ARM64_V8A).updateCheck(context) }
        assertFalse(actual.isUpdateAvailable)
    }

    @Test
    fun `negative update check for up-to-date app (ARMEABI_V7A)`() {
        makeReleaseJsonObjectAvailable()
        packageInfo.versionName = "98.1.0"
        val actual = runBlocking { createSut(ABI.ARMEABI_V7A).updateCheck(context) }
        assertFalse(actual.isUpdateAvailable)
    }

    @Test
    fun `negative update check for up-to-date app (X86)`() {
        makeReleaseJsonObjectAvailable()
        packageInfo.versionName = "98.1.0"
        val actual = runBlocking { createSut(ABI.X86).updateCheck(context) }
        assertFalse(actual.isUpdateAvailable)
    }

    @Test
    fun `negative update check for up-to-date app (X86_64)`() {
        makeReleaseJsonObjectAvailable()
        packageInfo.versionName = "98.1.0"
        val actual = runBlocking { createSut(ABI.X86_64).updateCheck(context) }
        assertFalse(actual.isUpdateAvailable)
    }

    @Test
    fun `positive update check for outdated app (ARM64_V8A)`() {
        makeReleaseJsonObjectAvailable()
        packageInfo.versionName = "97.2.0"
        val actual = runBlocking { createSut(ABI.ARM64_V8A).updateCheck(context) }
        assertTrue(actual.isUpdateAvailable)
    }

    @Test
    fun `positive update check for outdated app (ARMEABI_V7A)`() {
        makeReleaseJsonObjectAvailable()
        packageInfo.versionName = "97.2.0"
        val actual = runBlocking { createSut(ABI.ARMEABI_V7A).updateCheck(context) }
        assertTrue(actual.isUpdateAvailable)
    }

    @Test
    fun `positive update check for outdated app (X86)`() {
        makeReleaseJsonObjectAvailable()
        packageInfo.versionName = "97.2.0"
        val actual = runBlocking { createSut(ABI.X86).updateCheck(context) }
        assertTrue(actual.isUpdateAvailable)
    }

    @Test
    fun `positive update check for outdated app (X86_64)`() {
        makeReleaseJsonObjectAvailable()
        packageInfo.versionName = "97.2.0"
        val actual = runBlocking { createSut(ABI.X86_64).updateCheck(context) }
        assertTrue(actual.isUpdateAvailable)
    }
}