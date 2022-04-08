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
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.FileReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

class BraveIT {
    @MockK
    lateinit var context: Context

    @MockK
    lateinit var packageManager: PackageManager

    @MockK
    lateinit var apiConsumer: ApiConsumer

    val packageInfo = PackageInfo()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { context.packageManager } returns packageManager
        every { context.getString(R.string.available_version, any()) } returns "/"
        every { packageManager.getPackageInfo(App.BRAVE.detail.packageName, any()) } returns packageInfo
    }

    companion object {
        const val API_URl = "https://api.github.com/repos/brave/brave-browser/releases"
        const val DOWNLOAD_URL = "https://github.com/brave/brave-browser/releases/download"
        const val EXPECTED_VERSION = "1.20.103"
        val EXPECTED_RELEASE_TIMESTAMP = ZonedDateTime.parse("2021-02-10T11:30:45Z", ISO_ZONED_DATE_TIME)
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

    @Test
    fun `check download info ARMEABI_V7A - 2 network requests required`() {
        prepareNetworkForReleaseAfterTwoRequests()
        val result = runBlocking { createSut(ABI.ARMEABI_V7A).updateCheck(context) }
        assertEquals("$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonoarm.apk", result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(100446537L, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @Test
    fun `check download info ARM64_V8A - 2 network requests required`() {
        prepareNetworkForReleaseAfterTwoRequests()
        val result = runBlocking { createSut(ABI.ARM64_V8A).updateCheck(context) }
        assertEquals("$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonoarm64.apk", result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(171400033L, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @Test
    fun `check download info X86 - 2 network requests required`() {
        prepareNetworkForReleaseAfterTwoRequests()
        val result = runBlocking { createSut(ABI.X86).updateCheck(context) }
        assertEquals("$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonox86.apk", result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(131114604L, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @Test
    fun `check download info X86_64 - 2 network requests required`() {
        prepareNetworkForReleaseAfterTwoRequests()
        val result = runBlocking { createSut(ABI.X86_64).updateCheck(context) }
        assertEquals("$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonox64.apk", result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(201926660L, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @Test
    fun `check download info ARMEABI_V7A - 3 network requests required`() {
        prepareNetworkForReleaseAfterThreeRequests()
        val result = runBlocking { createSut(ABI.ARMEABI_V7A).updateCheck(context) }
        assertEquals("$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonoarm.apk", result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(100446537L, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @Test
    fun `check download info ARM64_V8A - 3 network requests required`() {
        prepareNetworkForReleaseAfterThreeRequests()
        val result = runBlocking { createSut(ABI.ARM64_V8A).updateCheck(context) }
        assertEquals("$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonoarm64.apk", result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(171400033L, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @Test
    fun `check download info X86 - 3 network requests required`() {
        prepareNetworkForReleaseAfterThreeRequests()
        val result = runBlocking { createSut(ABI.X86).updateCheck(context) }
        assertEquals("$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonox86.apk", result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(131114604L, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

    @Test
    fun `check download info X86_64 - 3 network requests required`() {
        prepareNetworkForReleaseAfterThreeRequests()
        val result = runBlocking { createSut(ABI.X86_64).updateCheck(context) }
        assertEquals("$DOWNLOAD_URL/v$EXPECTED_VERSION/BraveMonox64.apk", result.downloadUrl)
        assertEquals(EXPECTED_VERSION, result.version)
        assertEquals(201926660L, result.fileSizeBytes)
        assertEquals(EXPECTED_RELEASE_TIMESTAMP, result.publishDate)
    }

//    @Test
//    fun updateCheck_2releases_updateCheck() {
//        makeJsonObjectAvailable("latest_contains_NOT_release_version.json", "$API_URl/latest")
//        makeJsonArrayAvailable(
//            "2releases_perpage_20_page_1.json",
//            "$API_URl?per_page=20&page=1"
//        )
//
//        val packageInfo = PackageInfo()
//        every { packageManager.getPackageInfo(App.BRAVE.detail.packageName, any()) } returns packageInfo
//
//        // installed app is up-to-date
//        runBlocking {
//            packageInfo.versionName = "1.20.103"
//            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
//            assertFalse(actual.isUpdateAvailable)
//            assertEquals("1.20.103", actual.version)
//            assertEquals(100446537L, actual.fileSizeBytes)
//            assertEquals(
//                ZonedDateTime.parse("2021-02-10T11:30:45Z", ISO_ZONED_DATE_TIME),
//                actual.publishDate
//            )
//        }
//
//        // installed app is old
//        runBlocking {
//            packageInfo.versionName = "1.18.12"
//            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
//            assertTrue(actual.isUpdateAvailable)
//            assertEquals("1.20.103", actual.version)
//            assertEquals(100446537L, actual.fileSizeBytes)
//            assertEquals(
//                ZonedDateTime.parse("2021-02-10T11:30:45Z", ISO_ZONED_DATE_TIME),
//                actual.publishDate
//            )
//        }
//    }
//
//    @Test
//    fun updateCheck_3releases_checkDownloadUrlForABI() {
//        makeJsonObjectAvailable("latest_contains_NOT_release_version.json", "$API_URl/latest")
//        makeJsonArrayAvailable(
//            "3releases_perpage_10_page_1.json",
//            "$API_URl?per_page=20&page=1"
//        )
//        makeJsonArrayAvailable(
//            "3releases_perpage_10_page_2.json",
//            "$API_URl?per_page=20&page=2"
//        )
//
//        val packageInfo = PackageInfo()
//        every { packageManager.getPackageInfo(App.BRAVE.detail.packageName, any()) } returns packageInfo
//
//        runBlocking {
//            assertEquals(
//                "$DOWNLOAD_URL/v1.20.103/BraveMonoarm.apk",
//                createSut(ABI.ARMEABI_V7A).updateCheck(context).downloadUrl
//            )
//        }
//
//        runBlocking {
//            assertEquals(
//                "$DOWNLOAD_URL/v1.20.103/BraveMonoarm64.apk",
//                createSut(ABI.ARM64_V8A).updateCheck(context).downloadUrl
//            )
//        }
//
//        runBlocking {
//            assertEquals(
//                "$DOWNLOAD_URL/v1.20.103/BraveMonox86.apk",
//                createSut(ABI.X86).updateCheck(context).downloadUrl
//            )
//        }
//
//        runBlocking {
//            assertEquals(
//                "$DOWNLOAD_URL/v1.20.103/BraveMonox64.apk",
//                createSut(ABI.X86_64).updateCheck(context).downloadUrl
//            )
//        }
//    }
//
//    @Test
//    fun updateCheck_3releases_updateCheck() {
//        makeJsonObjectAvailable("latest_contains_NOT_release_version.json", "$API_URl/latest")
//        makeJsonArrayAvailable(
//            "3releases_perpage_10_page_1.json",
//            "$API_URl?per_page=20&page=1"
//        )
//        makeJsonArrayAvailable(
//            "3releases_perpage_10_page_2.json",
//            "$API_URl?per_page=20&page=2"
//        )
//
//        val packageInfo = PackageInfo()
//        every { packageManager.getPackageInfo(App.BRAVE.detail.packageName, any()) } returns packageInfo
//
//        // installed app is up-to-date
//        runBlocking {
//            packageInfo.versionName = "1.20.103"
//            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
//            assertFalse(actual.isUpdateAvailable)
//            assertEquals("1.20.103", actual.version)
//            assertEquals(100446537L, actual.fileSizeBytes)
//            assertEquals(
//                ZonedDateTime.parse("2021-02-10T11:30:45Z", ISO_ZONED_DATE_TIME),
//                actual.publishDate
//            )
//        }
//
//        // installed app is old
//        runBlocking {
//            packageInfo.versionName = "1.18.12"
//            val actual = createSut(ABI.ARMEABI_V7A).updateCheck(context)
//            assertTrue(actual.isUpdateAvailable)
//            assertEquals("1.20.103", actual.version)
//            assertEquals(100446537L, actual.fileSizeBytes)
//            assertEquals(
//                ZonedDateTime.parse("2021-02-10T11:30:45Z", ISO_ZONED_DATE_TIME),
//                actual.publishDate
//            )
//        }
//    }
}