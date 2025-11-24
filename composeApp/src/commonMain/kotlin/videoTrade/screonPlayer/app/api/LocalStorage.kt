/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.api

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

private val settings = Settings()

    fun addValueInStorage(key: String, value: String) {
        settings.putString(key, value)
    }

    fun getValueInStorage(key: String): String? {
        val value = settings.getString(key, defaultValue = "error")

        if (value === "error")
            return null

        return value
    }

fun delValueInStorage(key: String) {
    settings.remove(key)
}


private fun getCacheFilePath(key: String): File {
    val userHome = System.getProperty("user.home")
    val dir = Paths.get(userHome, ".screonPlayer", "cache").toFile().apply {
        if (!exists()) mkdirs()
    }
    return File(dir, "$key.json")
}

suspend fun safeAddValueInStorage(key: String, value: String) {
    try {
        addValueInStorage(key, value)
    } catch (e: IllegalArgumentException) {
        println("Preferences put failed (value too long), falling back to file cache: $e")
        try {
            getCacheFilePath(key).writeText(value)
        } catch (f: Exception) {
            println("Failed to write fallback cache file for key=$key: $f")
        }
    }
}

private const val PB_INDEX = "pb_index"

suspend fun addPbKeyToIndex(key: String) {
    val current: Set<String> = getValueInStorage(PB_INDEX)
        ?.let { runCatching { Json.decodeFromString<Set<String>>(it) }.getOrNull() }
        ?: emptySet()
    val next = (current + key).toSet()
    safeAddValueInStorage(PB_INDEX, Json.encodeToString(next))
}

fun getPbIndex(): Set<String> {
    return getValueInStorage(PB_INDEX)
        ?.let { runCatching { Json.decodeFromString<Set<String>>(it) }.getOrNull() }
        ?: emptySet()
}

fun clearPbIndex() {
    delValueInStorage(PB_INDEX)
}