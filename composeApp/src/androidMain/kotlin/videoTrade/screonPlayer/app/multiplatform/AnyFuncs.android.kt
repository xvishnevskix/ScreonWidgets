/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import videoTrade.screonPlayer.app.androidApi.ContextObj
import java.util.UUID


actual fun getPlatform(): Platform = Platform.Android

fun getOrCreateAppInstanceId(context: Context): String {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val id = prefs.getString("app_instance_id", null)
    
    return if (id == null) {
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString("app_instance_id", newId).apply()
        Log.d("AppInstanceID", "Generated new app instance ID: $newId")
        newId
    } else {
        Log.d("AppInstanceID", "Loaded existing app instance ID: $id")
        id
    }
}

actual fun getDeviceInfo(): DeviceInfo? {
    val context = ContextObj.getContext()
    return DeviceInfo(
        serialNumber = getOrCreateAppInstanceId(context),
        model = Build.MODEL,
        os = "ANDROID"
    )
}

actual fun decodeImageBitmap(filePath: String): ImageBitmap {
    // Декодируем Android-Bitmap из файла
    val bmp = BitmapFactory.decodeFile(filePath)
        ?: throw IllegalArgumentException("Cannot decode image at $filePath")
    // Конвертируем в Compose ImageBitmap
    return bmp.asImageBitmap()
}