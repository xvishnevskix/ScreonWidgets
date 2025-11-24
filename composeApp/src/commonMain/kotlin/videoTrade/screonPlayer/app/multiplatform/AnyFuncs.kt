/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import androidx.compose.ui.graphics.ImageBitmap

enum class Platform {
    Desktop,
    Android
}

expect fun getPlatform(): Platform


expect fun getDeviceInfo(): DeviceInfo?


data class DeviceInfo(
    val serialNumber: String,
    val model: String,
    val os: String
)

expect fun decodeImageBitmap(filePath: String): ImageBitmap