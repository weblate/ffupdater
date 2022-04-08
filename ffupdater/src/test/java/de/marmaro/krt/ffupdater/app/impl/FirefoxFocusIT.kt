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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.FileReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import java.util.stream.Stream

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

        val url = "https://api.github.com/repos/mozilla-mobile/focus-android/releases/latest"
        val path = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/FirefoxFocus/latest.json"
        coEvery {
            apiConsumer.consumeNetworkResource(url, GithubConsumer.Release::class)
        } returns Gson().fromJson(FileReader(path), GithubConsumer.Release::class.java)
    }

    companion object {
        const val DOWNLOAD_URL = "https://github.com/mozilla-mobile/focus-android/releases/download"
        const val EXPECTED_VERSION = "99.1.1"
        val EXPECTED_RELEASE_TIMESTAMP: ZonedDateTime =
            ZonedDateTime.parse("2022-03-31T05:06:42Z", ISO_ZONED_DATE_TIME)

        @JvmStatic
        fun abisWithMetaData(): Stream<Arguments> = Stream.of(
            Arguments.of(ABI.ARMEABI_V7A, "$DOWNLOAD_URL/v99.1.1/focus-99.1.1-armeabi-v7a.apk", 66866488L),
            Arguments.of(ABI.ARM64_V8A, "$DOWNLOAD_URL/v99.1.1/focus-99.1.1-arm64-v8a.apk", 70704414L),
            Arguments.of(ABI.X86, "$DOWNLOAD_URL/v99.1.1/focus-99.1.1-x86.apk", 79547600L),
            Arguments.of(ABI.X86_64, "$DOWNLOAD_URL/v99.1.1/focus-99.1.1-x86_64.apk", 75627767L),
        )
    }

    private fun createSut(deviceAbi: ABI): FirefoxFocus {
        return FirefoxFocus(apiConsumer = apiConsumer, deviceAbis = listOf(deviceAbi))
    }

    @ParameterizedTest(name = "check download info for ABI \"{0}\"")
    @MethodSource("abisWithMetaData")
    fun `check download info for ABI X`(
        abi: ABI,
        url: String,
        fileSize: Long,
    ) {
        val result = runBlocking { createSut(abi).updateCheck(context) }
        Assertions.assertEquals(url, result.downloadUrl)
        Assertions.assertEquals(EXPECTED_VERSION, result.version)
        Assertions.assertEquals(fileSize, result.fileSizeBytes)
        Assertions.assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @ParameterizedTest(name = "update check for ABI \"{0}\" - outdated version installed")
    @MethodSource("abisWithMetaData")
    fun `update check for ABI X - outdated version installed`(
        abi: ABI,
    ) {
        packageInfo.versionName = "97.2.0"
        val result = runBlocking { createSut(abi).updateCheck(context) }
        Assertions.assertTrue(result.isUpdateAvailable)
    }

    @ParameterizedTest(name = "update check for ABI \"{0}\" - latest version installed")
    @MethodSource("abisWithMetaData")
    fun `update check for ABI X - latest version installed`(
        abi: ABI,
    ) {
        packageInfo.versionName = EXPECTED_VERSION
        val result = runBlocking { createSut(abi).updateCheck(context) }
        Assertions.assertFalse(result.isUpdateAvailable)
    }
}