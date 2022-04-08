package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.google.gson.Gson
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.impl.fetch.ApiConsumer
import de.marmaro.krt.ffupdater.app.impl.fetch.github.GithubConsumer.Release
import de.marmaro.krt.ffupdater.device.ABI
import io.mockk.*
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
class BraveIT {
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
        every { packageManager.getPackageInfo(App.BRAVE.detail.packageName, any()) } returns packageInfo
    }

    companion object {
        const val API_URl = "https://api.github.com/repos/brave/brave-browser/releases"
        const val DOWNLOAD_URL = "https://github.com/brave/brave-browser/releases/download"
        const val EXPECTED_VERSION = "1.20.103"
        val EXPECTED_RELEASE_TIMESTAMP: ZonedDateTime =
            ZonedDateTime.parse("2021-02-10T11:30:45Z", ISO_ZONED_DATE_TIME)

        @JvmStatic
        fun abisWithMetaData(): Stream<Arguments> = Stream.of(
            Arguments.of(ABI.ARMEABI_V7A, 2, "$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonoarm.apk", 100446537L),
            Arguments.of(ABI.ARMEABI_V7A, 3, "$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonoarm.apk", 100446537L),
            Arguments.of(ABI.ARM64_V8A, 2, "$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonoarm64.apk", 171400033L),
            Arguments.of(ABI.ARM64_V8A, 3, "$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonoarm64.apk", 171400033L),
            Arguments.of(ABI.X86, 2, "$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonox86.apk", 131114604L),
            Arguments.of(ABI.X86, 3, "$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonox86.apk", 131114604L),
            Arguments.of(ABI.X86_64, 2, "$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonox64.apk", 201926660L),
            Arguments.of(ABI.X86_64, 3, "$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonox64.apk", 201926660L),
        )
    }

    private fun createSut(deviceAbi: ABI): Brave {
        return Brave(apiConsumer = apiConsumer, deviceAbis = listOf(deviceAbi))
    }

    private fun prepareNetworkForReleaseAfterTwoRequests() {
        val basePath = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/Brave"

        coEvery {
            apiConsumer.consumeNetworkResource("$API_URl/latest", Release::class)
        } returns Gson().fromJson(
            FileReader("$basePath/latest_contains_NOT_release_version.json"),
            Release::class.java
        )

        coEvery {
            apiConsumer.consumeNetworkResource("$API_URl?per_page=20&page=1", Array<Release>::class)
        } returns Gson().fromJson(
            FileReader("$basePath/2releases_perpage_20_page_1.json"),
            Array<Release>::class.java
        )
    }

    private fun prepareNetworkForReleaseAfterThreeRequests() {
        val basePath = "src/test/resources/de/marmaro/krt/ffupdater/app/impl/Brave"

        coEvery {
            apiConsumer.consumeNetworkResource("$API_URl/latest", Release::class)
        } returns Gson().fromJson(
            FileReader("$basePath/latest_contains_NOT_release_version.json"),
            Release::class.java
        )

        coEvery {
            apiConsumer.consumeNetworkResource("$API_URl?per_page=20&page=1", Array<Release>::class)
        } returns Gson().fromJson(
            FileReader("$basePath/3releases_perpage_10_page_1.json"),
            Array<Release>::class.java
        )

        coEvery {
            apiConsumer.consumeNetworkResource("$API_URl?per_page=20&page=2", Array<Release>::class)
        } returns Gson().fromJson(
            FileReader("$basePath/3releases_perpage_10_page_2.json"),
            Array<Release>::class.java
        )
    }

    @ParameterizedTest(name = "check download info for ABI \"{0}\" - \"{1}\" network requests required")
    @MethodSource("abisWithMetaData")
    fun `check download info for ABI X - X network requests required`(
        abi: ABI,
        networkRequests: Int,
        url: String,
        fileSize: Long,
    ) {
        when (networkRequests) {
            2 -> prepareNetworkForReleaseAfterTwoRequests()
            3 -> prepareNetworkForReleaseAfterThreeRequests()
            else -> throw IllegalStateException()
        }
        val result = runBlocking { createSut(abi).updateCheck(context) }
        assertEquals(url, result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(fileSize, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @ParameterizedTest(name = "update check for ABI \"{0}\" - outdated version installed - \"{1}\" network requests required")
    @MethodSource("abisWithMetaData")
    fun `update check for ABI X - outdated version installed - X network requests required`(
        abi: ABI,
        networkRequests: Int,
    ) {
        when (networkRequests) {
            2 -> prepareNetworkForReleaseAfterTwoRequests()
            3 -> prepareNetworkForReleaseAfterThreeRequests()
            else -> throw IllegalStateException()
        }
        packageInfo.versionName = "1.18.12"
        val result = runBlocking { createSut(abi).updateCheck(context) }
        assertTrue(result.isUpdateAvailable)
    }

    @ParameterizedTest(name = "update check for ABI \"{0}\" - latest version installed - \"{1}\" network requests required")
    @MethodSource("abisWithMetaData")
    fun `update check for ABI X - latest version installed - X network requests required`(
        abi: ABI,
        networkRequests: Int,
    ) {
        when (networkRequests) {
            2 -> prepareNetworkForReleaseAfterTwoRequests()
            3 -> prepareNetworkForReleaseAfterThreeRequests()
            else -> throw IllegalStateException()
        }
        packageInfo.versionName = "1.20.103"
        val result = runBlocking { createSut(abi).updateCheck(context) }
        assertFalse(result.isUpdateAvailable)
    }
}