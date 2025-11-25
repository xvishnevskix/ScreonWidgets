/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.util.decodeBase64Bytes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import videoTrade.screonPlayer.app.domain.model.VideoItem
import java.io.File
import videoTrade.screonPlayer.app.api.PathConfig.SERVER_URL
import videoTrade.screonPlayer.app.domain.model.FileItem
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.mapNotNull
import kotlin.io.copyTo
import kotlin.io.outputStream
import kotlin.io.use
import kotlin.ranges.coerceIn
import kotlin.runCatching
import kotlin.text.lowercase
import kotlin.text.substringAfterLast
import kotlin.text.substringBeforeLast

private lateinit var appContext: Context


private val SUPPORTED_VIDEO_EXTENSIONS =
    setOf("mp4", "mkv", "avi", "mov", "webm")

actual class FileProvider {

    private val client = HttpClientProvider.client


    actual suspend fun downloadVideoById(
        fileItem: FileItem,
        saveToFolder: String,
        onProgress: (Float) -> Unit
    ): VideoItem? {
        val fileId = fileItem.fileId
        val fileName = "$fileId.mp4"
        val saveDir = File(appContext.filesDir, saveToFolder).apply { mkdirs() }
        val saveFile = File(saveDir, fileName)
        val tmpFile = File(saveDir, "$fileName.part")

        try {
            val url = "${SERVER_URL}files/$fileId/download"

            // Делаем запрос и узнаём ожидаемый размер
            println("Делаем запрос и узнаём ожидаемый размер")
            return HttpClientProvider.client.prepareGet(url).execute { response ->
                if (!response.status.isSuccess()) {
                    println("❌ HTTP ${response.status} при скачивании $fileId")
                    return@execute null
                }
                val totalBytes = response.contentLength() ?: -1L

                println("${totalBytes} байтов")
                // Если готовый файл уже есть — валидируем по размеру
                if (saveFile.exists()) {
                    if (totalBytes > 0 && saveFile.length() == totalBytes) {
                        println("Размер совпал, перекачка не нужна")
                        // Размер совпал — считаем файл валидным, перекачка не нужна
                        println("📂 Файл уже существует и валиден: ${saveFile.absolutePath}")
                        onProgress(1f)
                        return@execute VideoItem(
                            title      = fileItem.name,
                            filePath   = saveFile.absolutePath,
                            type       = fileItem.type,
                            duration   = fileItem.duration,
                            orderIndex = fileItem.orderIndex ?: 0,
                        )
                    } else {
                        // Размер неизвестен или не совпал — удаляем и качаем заново
                        println("⚠️ Найден существующий файл, но размер не совпадает (have=${saveFile.length()}, expected=$totalBytes). Перекачиваем.")
                        runCatching { saveFile.delete() }
                    }
                }

                // Подготавливаем временный файл (чтобы не оставлять битые файлы)
                if (tmpFile.exists()) {
                    println("Подготавливаем временный файл")
                    // очищаем старый .part
                    runCatching { tmpFile.delete() }
                }

                val channel: ByteReadChannel = response.body()
                println("⬇️ Загрузка $fileId, размер: ${if (totalBytes > 0) "${totalBytes / 1024} KB" else "неизвестен"}")

                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesCopied = 0L
                var lastProgress = -1

                tmpFile.outputStream().use { output ->
                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead == -1) break
                        output.write(buffer, 0, bytesRead)
                        bytesCopied += bytesRead

                        if (totalBytes > 0) {
                            val progress = (bytesCopied.toDouble() / totalBytes).coerceIn(0.0, 1.0).toFloat()
                            val rounded = (progress * 100).toInt()
                            if (rounded != lastProgress) {
                                lastProgress = rounded
                                onProgress(progress)
                            }
                        }
                    }
                }

                // сверяем
                if (totalBytes > 0 && bytesCopied != totalBytes) {
                    println("❌ Неполная загрузка: получено $bytesCopied из $totalBytes байт")
                    runCatching { tmpFile.delete() }
                    return@execute null
                }

                if (!tmpFile.renameTo(saveFile)) {
                    tmpFile.copyTo(saveFile, overwrite = true)
                    tmpFile.delete()
                }

                onProgress(1f)
                println("✅ Скачано и сохранено: ${saveFile.absolutePath}")

                VideoItem(
                    title      = fileItem.name,
                    filePath   = saveFile.absolutePath,
                    type       = fileItem.type,
                    duration   = fileItem.duration,
                    orderIndex = fileItem.orderIndex ?: 0,
                )
            }
        } catch (e: Exception) {
            println("❌ Ошибка загрузки файла $fileId: ${e.message}")
            // обязательно удаляем временный .part
            runCatching { File(saveDir, "$fileName.part").delete() }
            return null
        }
    }

    fun initLocalVideoProvider(context: Context) {
        appContext = context.applicationContext
        copyResFolderToInternalStorage(appContext, "videos_downloaded", "videos_downloaded")
    }

    fun copyResFolderToInternalStorage(
        context: Context,
        assetFolder: String,
        targetFolderName: String
    ): List<VideoItem> {
        val assetManager = context.assets
        val fileNames = assetManager.list(assetFolder) ?: return emptyList()

        val outputDir = File(context.filesDir, targetFolderName).apply { mkdirs() }

        return fileNames
            .filter { it.substringAfterLast('.').lowercase() in SUPPORTED_VIDEO_EXTENSIONS }
            .mapNotNull { name ->
                try {
                    val inputStream = assetManager.open("$assetFolder/$name")
                    val outFile = File(outputDir, name)

                    if (!outFile.exists()) {
                        inputStream.use { input ->
                            outFile.outputStream().use { input.copyTo(it) }
                        }
                    }

                    VideoItem(
                        title = name.substringBeforeLast("."),
                        filePath = outFile.absolutePath
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
    }


//    fun markFileUsed(path: String) {
//        runCatching { File(path).setLastModified(System.currentTimeMillis()) }
//    }

//    fun cleanupOldFiles(
//        baseDir: File,
//        folderName: String,
//        months: Long = 6,
//        onDeleted: (File) -> Unit = {}
//    ) {
//        val targetDir = File(baseDir, folderName)
//        if (!targetDir.exists()) return
//
//        // 6 месяцев назад (приблизительно, 30.44 дня на месяц)
//        val cutoffMillis = System.currentTimeMillis() - (months * 2629800000L) // 30.44 * 24 * 60 * 60 * 1000
//
//        targetDir.walkTopDown()
//            .maxDepth(1)
//            .filter { it.isFile }
//            .filter { file ->
//                val name = file.name.lowercase()
//                // чистим и .part, и обычные файлы
//                name.endsWith(".part") ||
//                        name.substringAfterLast('.', "").let { ext -> ext in SUPPORTED_VIDEO_EXTENSIONS } &&
//                        file.lastModified() < cutoffMillis
//            }
//            .forEach { file ->
//                runCatching { file.delete() }
//                    .onSuccess { onDeleted(file) }
//            }
//    }

    // использование
    //cleanupOldFiles(appContext.filesDir, "videos_downloaded", months = 6)
}

actual object FileProviderFactory {

    actual fun create(): FileProvider {
        return FileProvider()
    }
}
