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
import org.junit.jupiter.api.Assertions.*
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
class BromiteIT {
    @MockK
    lateinit var context: Context

    @MockK
    lateinit var packageManager: PackageManager

    @MockK
    lateinit var apiConsumer: ApiConsumer

    val packageInfo = PackageInfo()

    @BeforeEach
    fun setUp() {
        every { context.packageManager } returns packageManager
        every { context.getString(R.string.available_version, any()) } returns "/"
        every { packageManager.getPackageInfo(App.BROMITE.detail.packageName, 0) } returns packageInfo
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
        const val EXPECTED_VERSION = "100.0.4896.57"
        val EXPECTED_RELEASE_TIMESTAMP: ZonedDateTime =
            ZonedDateTime.parse("2022-03-29T21:36:18Z", ISO_ZONED_DATE_TIME)

        @JvmStatic
        fun abisWithMetaData(): Stream<Arguments> = Stream.of(
            Arguments.of(ABI.ARMEABI_V7A, "$DOWNLOAD_URL/arm_ChromePublic.apk", 118003761L),
            Arguments.of(ABI.ARM64_V8A, "$DOWNLOAD_URL/arm64_ChromePublic.apk", 157594854L),
            Arguments.of(ABI.X86, "$DOWNLOAD_URL/x86_ChromePublic.apk", 156124169L),
            Arguments.of(ABI.X86_64, "$DOWNLOAD_URL/x64_ChromePublic.apk", 168821833L),
        )
    }

    private fun createSut(deviceAbi: ABI): Bromite {
        return Bromite(apiConsumer = apiConsumer, deviceAbis = listOf(deviceAbi))
    }

    @ParameterizedTest(name = "check download info for ABI \"{0}\"")
    @MethodSource("abisWithMetaData")
    fun `check download info for ABI X`(
        abi: ABI,
        url: String,
        fileSize: Long,
    ) {
        val result = runBlocking { createSut(abi).updateCheck(context) }
        assertEquals(url, result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(fileSize, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @ParameterizedTest(name = "update check for ABI \"{0}\" - outdated version installed")
    @MethodSource("abisWithMetaData")
    fun `update check for ABI X - outdated version installed`(
        abi: ABI,
    ) {
        packageInfo.versionName = "89.0.4389.117"
        val result = runBlocking { createSut(abi).updateCheck(context) }
        assertTrue(result.isUpdateAvailable)
    }

    @ParameterizedTest(name = "update check for ABI \"{0}\" - latest version installed")
    @MethodSource("abisWithMetaData")
    fun `update check for ABI X - latest version installed`(
        abi: ABI,
    ) {
        packageInfo.versionName = "100.0.4896.57"
        val result = runBlocking { createSut(abi).updateCheck(context) }
        assertFalse(result.isUpdateAvailable)
    }
}