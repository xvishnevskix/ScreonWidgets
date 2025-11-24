/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.presentation.screens.player

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import videoTrade.screonPlayer.app.domain.model.VideoItem
import videoTrade.screonPlayer.app.domain.repository.VideoPlayer


class VideoPlayerViewModel(
    private val player: VideoPlayer,

) : ViewModel(), KoinComponent {

    private val _playlist = mutableStateListOf<VideoItem>()
    val playlist: List<VideoItem> get() = _playlist

    private val _isReadyToPlay = mutableStateOf(false)
    val isReadyToPlay: State<Boolean> get() = _isReadyToPlay

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> get() = _isPlaying


    private var currentIndex by mutableStateOf(0)

    private var pendingPlaylist: List<VideoItem>? = null
    private var pendingRecurring: Boolean? = null
    private var pendingOnFinish: (() -> Unit)? = null


    private val _resumeImageLeftMs = mutableStateOf<Long?>(null)
    fun requestResumeImageLeft(ms: Long) { _resumeImageLeftMs.value = ms }
    fun consumeResumeImageLeft(): Long? = _resumeImageLeftMs.value.also { _resumeImageLeftMs.value = null }

    val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    private var currentItemStartedAtMs: Long = 0L
    private var currentItemIsVideo: Boolean = true

    fun stageNextPlaylist(
        videos: List<VideoItem>,
        recurring: Boolean,
        onFinish: () -> Unit
    ) {
        pendingPlaylist = videos
        pendingRecurring = recurring
        pendingOnFinish = onFinish
    }

    // Возвращает true, если произошла подмена
    fun applyPendingIfAny(): Boolean {
        val next = pendingPlaylist ?: return false
        setSlotRecurring(pendingRecurring ?: false)
        setOnPlaylistFinishedCallback(pendingOnFinish ?: {})

        clearPlaylist()
        addVideos(next)
        select(0)

        pendingPlaylist = null
        pendingRecurring = null
        pendingOnFinish = null
        return true
    }

    fun cutToPendingNow() {
        // обрываем текущее воспроизведение
        player.stop()
        // тут же подменяем на staged плейлист
        applyPendingIfAny()

        player.forceRecreateVout()
    }


    val currentVideo: State<VideoItem?> = derivedStateOf {
//        println("playlist $playlist")
        playlist.getOrNull(currentIndex)
    }




    private val _resumeFromFraction = mutableStateOf<Float?>(null)
    fun requestResumeFromFraction(frac: Float) { _resumeFromFraction.value = frac }
    fun consumeResumeRequest(): Float? = _resumeFromFraction.value.also { _resumeFromFraction.value = null }

    private val _playbackTrigger = mutableStateOf(0)
    val playbackTrigger: State<Int> get() = _playbackTrigger
    fun nudgePlaybackTrigger() { _playbackTrigger.value += 1 }

    private val _isCurrentSlotRecurring = mutableStateOf(false)
    val isCurrentSlotRecurring: State<Boolean> get() = _isCurrentSlotRecurring


    fun setSlotRecurring(recurring: Boolean) {
        _isCurrentSlotRecurring.value = recurring
    }

    private var onOneTimePlaylistFinished: () -> Unit = {}

    fun setOnPlaylistFinishedCallback(cb: () -> Unit) {
        onOneTimePlaylistFinished = cb
    }

    fun next() {
        // если есть заготовленный новый плейлист — подменяем сразу
        if (applyPendingIfAny()) return

        if (_playlist.isEmpty()) return
        val lastIndex = _playlist.lastIndex
        val recurring = _isCurrentSlotRecurring.value

        if (_playlist.size > 1) {
            when {
                currentIndex < lastIndex -> currentIndex += 1
                recurring -> currentIndex = 0
                else -> onOneTimePlaylistFinished()
            }
        } else {
            if (recurring) {
                _playbackTrigger.value += 1
            } else {
                onOneTimePlaylistFinished()
            }
        }
    }

    fun playCurrent() {
        currentVideo.value?.let { item ->
            currentItemStartedAtMs = Clock.System.now().toEpochMilliseconds()
            currentItemIsVideo = item.type.startsWith("video/")
            if (currentItemIsVideo) {
                player.play(item.filePath)
            } else {
                player.stop()
                // картинка просто держится на экране n секунд
            }
        }
    }

    fun getCurrentItemElapsedMs(): Long {
        return if (currentItemIsVideo) getCurrentPositionMs()
        else (Clock.System.now().toEpochMilliseconds() - currentItemStartedAtMs)
    }

    fun togglePlayPause() {
        if (player.isPlaying()) {
            player.pause()
            _isPlaying.value = false
        } else {
            _isPlaying.value = true
            player.resume()
        }
    }

    fun pausePlayback() = player.pause()

    fun previous() {
        if (_playlist.isNotEmpty()) {
            currentIndex = if (currentIndex == 0) _playlist.lastIndex else currentIndex - 1
        }
    }

    suspend fun previousAndPlay() {
        previous()
        player.awaitReady()
        playCurrent()
    }

    fun select(index: Int) {
        if (index in _playlist.indices) {
            currentIndex = index
        }
    }


    fun addVideos(videos: List<VideoItem>) {
        _playlist.addAll(videos)
        _playlist.sortBy { it.orderIndex }
    }

    fun getCurrentPositionMs(): Long {
        val it = currentVideo.value ?: return 0L
        val durMs = (it.duration * 1000).toLong().coerceAtLeast(1L)
        val p = player.getProgress() // 0..1
        return (p * durMs).toLong()
    }


    fun clearPlaylist() {
        _playlist.clear()
    }
}
