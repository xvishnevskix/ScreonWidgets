/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import videoTrade.screonPlayer.app.domain.model.VideoItem

import kotlinx.coroutines.CompletableDeferred
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import videoTrade.screonPlayer.app.domain.repository.VideoPlayer
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.awt.event.HierarchyEvent
import java.io.File
import java.util.zip.ZipInputStream



class DesktopVideoPlayer : VideoPlayer {
    private val component = EmbeddedMediaPlayerComponent()
    private val surface = component.videoSurfaceComponent()
    private var onEndCallback: (() -> Unit)? = null
    private val surfaceReady = CompletableDeferred<Unit>()

    init {
        // 1) Повесим слушатель, чтобы разово закрыть deferred,
        //    когда surface впервые станет displayable
        surface.addHierarchyListener { evt ->
            if ((evt.changeFlags and HierarchyEvent.DISPLAYABILITY_CHANGED.toLong()) != 0L
                && surface.isDisplayable
                && !surfaceReady.isCompleted
            ) {
                surfaceReady.complete(Unit)
            }
        }

        // 2) Слушаем окончание медиаплеера
        component.mediaPlayer().events().addMediaPlayerEventListener(object: MediaPlayerEventAdapter() {
            override fun finished(mp: uk.co.caprica.vlcj.player.base.MediaPlayer?) {
                onEndCallback?.invoke()
            }
        })
    }

    override suspend fun awaitSurfaceReady() {
        surfaceReady.await()
    }

    override fun setOnEndCallback(callback: () -> Unit) {
        onEndCallback = callback
    }

    override suspend fun awaitReady() {
        surfaceReady.await()
    }
    override fun play(path: String) {
        val mp = component.mediaPlayer()

        // 1) Остановить старое
        mp.controls().stop()
        Thread.sleep(50)

        // 2) (если есть необходимость) «открепить» видео-сурфейс — обычно vlcj сам это делает
        //    но для надёжности можно вызвать
        component.videoSurfaceComponent().let {
            it.isVisible = false
            it.isVisible = true
        }

        // 3) Запустить новый
        mp.media().play(path)
    }

    override fun pause() = component.mediaPlayer().controls().pause()
    override fun resume() = component.mediaPlayer().controls().play()
    override fun stop() = component.mediaPlayer().controls().stop()
    override fun isPlaying(): Boolean = component.mediaPlayer().status().isPlaying
    override fun seek(position: Float) = component.mediaPlayer().controls().setPosition(position)
    override fun getProgress(): Float = component.mediaPlayer().status().position()
    override fun release() = component.release()

    fun awtComponent(): java.awt.Component = component.videoSurfaceComponent()
}
