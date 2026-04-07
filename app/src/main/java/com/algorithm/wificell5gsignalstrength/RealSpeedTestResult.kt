package com.algorithm.wificell5gsignalstrength

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt
import kotlin.random.Random

data class RealSpeedTestResult(
    val pingMs: Int,
    val downloadMbps: Float,
    val uploadMbps: Float
)

class RealSpeedTester {

    companion object {
        private const val DOWNLOAD_BASE = "https://speed.cloudflare.com/__down"
        private const val UPLOAD_URL = "https://speed.cloudflare.com/__up"

        private const val PING_ATTEMPTS = 5
        private const val DOWNLOAD_BYTES = 20_000_000
        private const val UPLOAD_BYTES = 5_000_000

        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 20_000
        private const val PROGRESS_INTERVAL_MS = 250L
    }

    suspend fun run(
        onDownloadProgress: (Float, Int) -> Unit,
        onUploadProgress: (Float, Int) -> Unit
    ): RealSpeedTestResult = withContext(Dispatchers.IO) {
        val ping = measurePingMs()

        val download = measureDownloadMbps(
            bytes = DOWNLOAD_BYTES,
            pingMs = ping,
            onProgress = onDownloadProgress
        )

        val upload = measureUploadMbps(
            bytes = UPLOAD_BYTES,
            pingMs = ping,
            onProgress = onUploadProgress
        )

        RealSpeedTestResult(
            pingMs = ping,
            downloadMbps = download,
            uploadMbps = upload
        )
    }

    private fun measurePingMs(): Int {
        val samples = mutableListOf<Long>()

        repeat(PING_ATTEMPTS) {
            val url = "$DOWNLOAD_BASE?bytes=1&ts=${System.nanoTime()}"
            val start = System.nanoTime()

            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                useCaches = false
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
            }

            try {
                connection.connect()
                connection.inputStream.use { input ->
                    input.read()
                }
                val elapsedMs = (System.nanoTime() - start) / 1_000_000
                if (elapsedMs > 0) samples += elapsedMs
            } finally {
                connection.disconnect()
            }
        }

        return if (samples.isEmpty()) 0 else (samples.average().roundToInt())
    }

    private fun measureDownloadMbps(
        bytes: Int,
        pingMs: Int,
        onProgress: (Float, Int) -> Unit
    ): Float {
        val url = "$DOWNLOAD_BASE?bytes=$bytes&ts=${System.nanoTime()}"
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            useCaches = false
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
        }

        var totalBytes = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val startNs = System.nanoTime()
        var lastEmitMs = 0L

        try {
            connection.connect()

            BufferedInputStream(connection.inputStream).use { input ->
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break

                    totalBytes += read
                    val elapsedMs = (System.nanoTime() - startNs) / 1_000_000
                    if (elapsedMs - lastEmitMs >= PROGRESS_INTERVAL_MS) {
                        lastEmitMs = elapsedMs
                        val mbps = bytesToMbps(totalBytes, elapsedMs)
                        onProgress(mbps, pingMs)
                    }
                }
            }
        } finally {
            connection.disconnect()
        }

        val totalElapsedMs = ((System.nanoTime() - startNs) / 1_000_000).coerceAtLeast(1)
        return bytesToMbps(totalBytes, totalElapsedMs)
    }

    private fun measureUploadMbps(
        bytes: Int,
        pingMs: Int,
        onProgress: (Float, Int) -> Unit
    ): Float {
        val payload = Random.Default.nextBytes(bytes)
        val connection = (URL(UPLOAD_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            useCaches = false
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setFixedLengthStreamingMode(payload.size)
            setRequestProperty("Content-Type", "application/octet-stream")
        }

        val chunkSize = 16 * 1024
        var sentBytes = 0L
        val startNs = System.nanoTime()
        var lastEmitMs = 0L

        try {
            connection.connect()

            BufferedOutputStream(connection.outputStream).use { output ->
                var offset = 0
                while (offset < payload.size) {
                    val toWrite = minOf(chunkSize, payload.size - offset)
                    output.write(payload, offset, toWrite)
                    offset += toWrite
                    sentBytes += toWrite

                    val elapsedMs = (System.nanoTime() - startNs) / 1_000_000
                    if (elapsedMs - lastEmitMs >= PROGRESS_INTERVAL_MS) {
                        lastEmitMs = elapsedMs
                        val mbps = bytesToMbps(sentBytes, elapsedMs)
                        onProgress(mbps, pingMs)
                    }
                }
                output.flush()
            }

            runCatching {
                connection.inputStream.use { it.readBytes() }
            }
        } finally {
            connection.disconnect()
        }

        val totalElapsedMs = ((System.nanoTime() - startNs) / 1_000_000).coerceAtLeast(1)
        return bytesToMbps(sentBytes, totalElapsedMs)
    }

    private fun bytesToMbps(bytes: Long, elapsedMs: Long): Float {
        if (elapsedMs <= 0L) return 0f
        val bits = bytes * 8.0
        val seconds = elapsedMs / 1000.0
        return ((bits / seconds) / 1_000_000.0).toFloat()
    }
}