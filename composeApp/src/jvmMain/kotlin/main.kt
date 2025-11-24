/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.context.startKoin
import videoTrade.screonPlayer.app.di.getJvmModules
import videoTrade.screonPlayer.app.di.getSharedModules
import videoTrade.screonPlayer.app.App
import videoTrade.screonPlayer.app.presentation.screens.DesktopVideoPlayerScreen

import java.awt.Dimension
import java.io.File


fun main() = application {

    val vlcPath = File(System.getProperty("user.dir"), "vlc").absolutePath
    System.setProperty("jna.library.path", vlcPath)

    startKoin {
        modules(getSharedModules() + getJvmModules())
    }

    val windowState = rememberWindowState(
        placement = WindowPlacement.Fullscreen
    )

    Window(
        title = "screonPlayer",
        onCloseRequest = ::exitApplication,
        state       = windowState,
        undecorated = true,             // убирает рамку и заголовок
        resizable   = false,
        alwaysOnTop    = true
    ) {
        window.minimumSize = Dimension(350, 600)
        App()
//        DesktopVideoPlayerScreen()
    }
}
