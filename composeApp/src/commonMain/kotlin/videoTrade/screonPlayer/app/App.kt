/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import videoTrade.screonPlayer.app.presentation.screens.player.VideoPlayerScreen
import videoTrade.screonPlayer.app.theme.AppTheme

@Preview
@Composable
internal fun App() = AppTheme {

    KoinContext {
        Navigator(
            VideoPlayerScreen()
//            TestScreen()
        ) { navigator ->
            SlideTransition(navigator)
        }
    }
}
