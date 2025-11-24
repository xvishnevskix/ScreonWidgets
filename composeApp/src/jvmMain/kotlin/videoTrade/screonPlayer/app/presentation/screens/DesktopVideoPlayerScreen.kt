/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import videoTrade.screonPlayer.app.domain.repository.VideoPlayer
import videoTrade.screonPlayer.app.presentation.screens.player.VideoPlayerViewModel
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester


@Composable
fun DesktopVideoPlayerScreen() {
    val player = koinInject<VideoPlayer>()
    val viewModel = koinInject<VideoPlayerViewModel>()
    val position = remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    val controlsVisible = remember { mutableStateOf(true) }
    val lastInteraction = remember { mutableStateOf(System.currentTimeMillis()) }

    fun registerInteraction() {
        lastInteraction.value = System.currentTimeMillis()
        controlsVisible.value = true
    }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                    registerInteraction()
                    true
                } else {
                    false
                }
            }
            .clickable {
                controlsVisible.value = !controlsVisible.value
                registerInteraction()
            }
    ) {
        // Видео (верхняя часть)
        SwingPanel(
            factory = {
                (player as videoTrade.screonPlayer.app.multiplatform.DesktopVideoPlayer).awtComponent()
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            background = Color.Black
        )

        // Контролы (нижняя часть)
        AnimatedVisibility(visible = controlsVisible.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        controlsVisible.value = !controlsVisible.value
                        registerInteraction()
                    }
                    .background(Color.DarkGray.copy(alpha = 0.7f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = position.value,
                    onValueChange = {
                        position.value = it
                        player.seek(it)
                        registerInteraction()
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        registerInteraction()
                        scope.launch { viewModel.previousAndPlay() }
                    }) { Text("⏪") }
                    Button(onClick = {
                        registerInteraction()
                        viewModel.togglePlayPause()
                    }) {
                        Text(if (viewModel.isPlaying.value) "⏸" else "▶")
                    }
                    Button(onClick = {
                        registerInteraction()
//                        scope.launch { viewModel.nextAndPlay() }
                    }) { Text("⏩") }
                }
            }
        }

        // Обновление прогресса
        LaunchedEffect(viewModel.currentVideo.value) {
            while (true) {
                delay(500)
                position.value = player.getProgress()
            }
        }

        // Автостарт
        LaunchedEffect(viewModel.isReadyToPlay.value) {
            if (viewModel.isReadyToPlay.value) {
                player.awaitReady()
                viewModel.playCurrent()
            }
        }

        // На окончание
        LaunchedEffect(Unit) {
            player.setOnEndCallback {
//                scope.launch { viewModel.nextAndPlay() }
            }
        }

//        // Авто-скрытие контролов через 5 сек
//        LaunchedEffect(Unit) {
//            while (true) {
//                delay(1000)
//                val now = System.currentTimeMillis()
//                if (now - lastInteraction.value > 5000) {
//                    controlsVisible.value = false
//                }
//            }
//        }
//        LaunchedEffect(Unit) {
//            focusRequester.requestFocus()
//        }
    }
}

