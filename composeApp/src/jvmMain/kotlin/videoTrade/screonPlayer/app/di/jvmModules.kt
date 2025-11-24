/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.di

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import org.koin.dsl.module
import videoTrade.screonPlayer.app.domain.repository.StatusReporter
import videoTrade.screonPlayer.app.domain.repository.VideoPlayer
import videoTrade.screonPlayer.app.multiplatform.DesktopStatusReporter
import videoTrade.screonPlayer.app.multiplatform.DesktopVideoPlayer

fun getJvmModules() = listOf(
    module {
        single<StatusReporter> { DesktopStatusReporter() }
        single<VideoPlayer> { DesktopVideoPlayer() }
        single<@Composable () -> Unit> {
            val player = get<VideoPlayer>() as DesktopVideoPlayer
            @Composable {
                SwingPanel(
                    factory = { player.awtComponent() },
                    modifier = Modifier.fillMaxSize(),
                    background = Color.Black
                )
            }
        }
    }
)