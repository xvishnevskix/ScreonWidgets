/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import videoTrade.screonPlayer.app.domain.model.VideoItem
import java.io.File
import java.util.zip.ZipInputStream
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import videoTrade.screonPlayer.app.api.PathConfig.SERVER_URL
import videoTrade.screonPlayer.app.domain.model.FileItem


private val SUPPORTED_VIDEO_EXTENSIONS =
    setOf("mp4", "mkv", "avi", "mov", "webm")

actual class FileProvider {

    actual suspend fun downloadVideoById(
        fileItem: FileItem,
        saveToFolder: String,
        onProgress: (Float) -> Unit
    ): VideoItem? {
        try {
            val client   = HttpClientProvider.client
            val fileId   = fileItem.fileId
            val fileName = "$fileId.mp4"

            // Папка в %USER_HOME%/saveToFolder
            val userHome = System.getProperty("user.home")
            val saveDir  = File(userHome, saveToFolder).apply { mkdirs() }
            val saveFile = File(saveDir, fileName)

            // Уже скачано?
            if (saveFile.exists()) {
                onProgress(1f)
                return VideoItem(
                    title      = fileItem.name,
                    filePath   = saveFile.absolutePath,
                    type       = fileItem.type,
                    duration   = fileItem.duration,
                    orderIndex = fileItem.orderIndex ?: 0
                )
            }

            val url      = "$SERVER_URL/files/$fileId/download"
            val response = client.get(url)
            // Проверяем статус
            if (!response.status.isSuccess()) {
                println("❌ HTTP ${response.status} при скачивании $fileId")
                return null
            }

            // Читаем тело как канал
            val channel    = response.body<ByteReadChannel>()
            val totalBytes = response.contentLength() ?: -1L
            println("⬇️ Загрузка $fileId, размер: ${totalBytes / 1024} KB")

            val buffer      = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytesCopied = 0L
            var lastPct    = -1

            saveFile.outputStream().use { out ->
                while (!channel.isClosedForRead) {
                    val r = channel.readAvailable(buffer)
                    if (r <= 0) break
                    out.write(buffer, 0, r)
                    bytesCopied += r

                    if (totalBytes > 0) {
                        val pct = ((bytesCopied.toFloat() / totalBytes) * 100).toInt()
                        if (pct != lastPct) {
                            lastPct = pct
                            onProgress(bytesCopied.toFloat() / totalBytes)
                        }
                    }
                }
            }

            onProgress(1f)
            println("✅ Скачано и сохранено: ${saveFile.absolutePath}")

            return VideoItem(
                title      = fileItem.name,
                filePath   = saveFile.absolutePath,
                type       = fileItem.type,
                duration   = fileItem.duration,
                orderIndex = fileItem.orderIndex ?: 0
            )
        } catch (e: Exception) {
            println("❌ Ошибка загрузки файла ${fileItem.fileId}: ${e.message}")
            return null
        }
    }

}




private fun getVideosFromResourcesFolder(folder: String): List<VideoItem> {
    val classLoader = object {}.javaClass.classLoader
    val jarUri = object {}.javaClass.protectionDomain.codeSource.location.toURI()
    val jarFile = File(jarUri)

    if (!jarFile.name.endsWith(".jar")) {
        // Для запуска не из jar (например, в IDE)
        val resourceUrl = classLoader.getResource(folder) ?: return emptyList()
        val dir = File(resourceUrl.toURI())
        return dir.listFiles { file -> file.extension.lowercase() in SUPPORTED_VIDEO_EXTENSIONS }
            ?.mapIndexed { index, file ->
                val tempFile = File.createTempFile("video$index", ".${file.extension}")
                file.inputStream()
                    .use { input -> tempFile.outputStream().use { input.copyTo(it) } }
                VideoItem(file.nameWithoutExtension, tempFile.absolutePath)
            } ?: emptyList()
    }

    // В JAR — считываем список файлов вручную
    val result = mutableListOf<VideoItem>()
    ZipInputStream(jarFile.inputStream()).use { zip ->
        var entry = zip.nextEntry
        var index = 0
        while (entry != null) {
            val name = entry.name
            if (!entry.isDirectory && name.startsWith("$folder/") &&
                name.substringAfterLast('.').lowercase() in SUPPORTED_VIDEO_EXTENSIONS
            ) {

                val ext = name.substringAfterLast('.')
                val tempFile = File.createTempFile("video$index", ".$ext")
                tempFile.outputStream().use { out -> zip.copyTo(out) }

                val displayName = name.removePrefix("$folder/").substringBeforeLast('.')
                result += VideoItem(displayName, tempFile.absolutePath)
                index++
            }
            entry = zip.nextEntry
        }
    }

    return result
}


actual object FileProviderFactory {

    actual fun create(): FileProvider {
        return FileProvider()
    }
}