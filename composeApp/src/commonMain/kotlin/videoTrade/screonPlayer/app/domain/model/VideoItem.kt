/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.domain.model

import kotlinx.serialization.Serializable


data class VideoItem(
    val title: String,
    val filePath: String,
    val type: String = "video/",
    val duration: Double = 30.0,
    val orderIndex: Int = 0,
)

@Serializable
data class PlaybackState(
    val playlistId: String,
    val slotSignature: String,      // "${slot.playlistId}|${slot.startDate}|${slot.startTime}"
    val itemIndex: Int,
    val positionMs: Long,
    val itemIsVideo: Boolean,
    val itemDurationMs: Long,
    val updatedAt: Long
)

@Serializable
data class FileItem(
    val id: String,
    val source: String? = null,
    val fileId: String? = null,
    val name: String,
    val type: String,
    val previewUrl: String,
    val size: Long? = null,
    var duration: Double = 0.0,
    val orderIndex: Int? = 0,
    val iptvName: String? = null,
    val iptvUrl: String? = null,
    val iptvLogo: String? = null
)