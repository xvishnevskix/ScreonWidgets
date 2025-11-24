/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO


actual fun getPlatform(): Platform = Platform.Desktop

actual fun getDeviceInfo(): DeviceInfo? {
    // Храним сгенерированный UUID в файле в домашней папке
    val idFile = File(System.getProperty("user.home"), ".screonplayer_device_id")
    val serial = if (idFile.exists()) {
        idFile.readText().trim()
    } else {
        val newId = UUID.randomUUID().toString()
        idFile.writeText(newId)
        newId
    }
    return DeviceInfo(
        serialNumber = serial,
        model        = System.getProperty("os.name") + " " + System.getProperty("os.arch"),
        os           = System.getProperty("os.name") + " " + System.getProperty("os.version")
    )
}

actual fun decodeImageBitmap(filePath: String): ImageBitmap {
    val buffered = ImageIO.read(File(filePath))
        ?: throw IllegalArgumentException("Cannot decode image at $filePath")
    return buffered.toComposeImageBitmap()
}