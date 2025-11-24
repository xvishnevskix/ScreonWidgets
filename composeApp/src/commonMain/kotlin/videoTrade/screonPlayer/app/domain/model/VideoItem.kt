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