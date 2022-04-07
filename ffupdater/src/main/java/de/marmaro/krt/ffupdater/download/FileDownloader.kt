package de.marmaro.krt.ffupdater

import android.net.TrafficStats
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.Buffer
import okio.ForwardingSource
import okio.Source
import okio.buffer
import ru.gildor.coroutines.okhttp.await
import java.io.File
import java.io.IOException
import java.net.UnknownHostException

class FileDownloader {
    private val trafficStatsThreadId = 10001
    var isRunning: Boolean = false
        private set
    var errorMessage: String? = null
        private set

    var onProgress: (progressInPercent: Int) -> Unit = @WorkerThread {}
    var onFailure: () -> Unit = @WorkerThread {}
    var onSuccess: () -> Unit = @WorkerThread {}

    @MainThread
    suspend fun downloadFile(url: String, file: File): Boolean {
        try {
            return withContext(Dispatchers.IO) {
                isRunning = true
                downloadFileInternal(url, file)
            }
        } finally {
            isRunning = false
        }
    }

    @WorkerThread
    private suspend fun downloadFileInternal(url: String, file: File): Boolean {
        require(url.startsWith("https://"))
        TrafficStats.setThreadStatsTag(trafficStatsThreadId)
        val client = createClient()
        val call = callUrl(client, url) ?: return false
        call.use { response ->
            val body = response.body
            if (!response.isSuccessful || body == null) {
                errorMessage = "HTTP code: ${response.code}"
                onFailure()
                return false
            }
            file.outputStream().buffered().use { fileWriter ->
                body.byteStream().buffered().use { responseReader ->
                    // this method blocks until download is finished
                    responseReader.copyTo(fileWriter)
                    onSuccess()
                    return true
                }
            }
        }
    }

    private suspend fun callUrl(client: OkHttpClient, url: String): Response? {
        val request = Request.Builder()
            .url(url)
            .build()
        return try {
            client.newCall(request)
                .await()
        } catch (e: UnknownHostException) {
            errorMessage = e.localizedMessage
            null
        }
    }

    private fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor { chain: Interceptor.Chain ->
                val original = chain.proceed(chain.request())
                val body = requireNotNull(original.body)
                original.newBuilder()
                    .body(ProgressResponseBody(body, this))
                    .build()
            }
            .build()
    }
}

internal class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val fileDownloader: FileDownloader
) : ResponseBody() {
    override fun contentType() = responseBody.contentType()
    override fun contentLength() = responseBody.contentLength()
    override fun source() = trackTransmittedBytes(responseBody.source()).buffer()

    private fun trackTransmittedBytes(source: Source): Source {
        return object : ForwardingSource(source) {
            private val sourceIsExhausted = -1L
            var totalBytesRead = 0L
            var totalProgress = -1

            private fun getProgress(): Int {
                if (contentLength() == 0L) {
                    return 0
                }
                return (100 * totalBytesRead / contentLength()).toInt()
            }

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                if (bytesRead != sourceIsExhausted) {
                    totalBytesRead += bytesRead
                }
                val progress = getProgress()
                if (progress != totalProgress) {
                    totalProgress = progress
                    fileDownloader.onProgress(progress)
                }
                return bytesRead
            }
        }
    }
}