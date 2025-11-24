/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.domain.repository

interface VideoPlayer {
    fun play(path: String)
    fun isPlaying(): Boolean
    fun pause()
    fun resume()
    fun stop()
    fun release()
    fun getProgress(): Float
    fun seek(position: Float)
    fun setOnEndCallback(callback: () -> Unit)
    suspend fun awaitReady() {}
    suspend fun awaitSurfaceReady() {}
    fun forceRecreateVout() {}
    fun setMuted(muted: Boolean) {}
    suspend fun awaitFirstFrame() {}
    fun setRotation(deg: Int)
}

