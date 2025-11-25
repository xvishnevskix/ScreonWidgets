/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.di

import org.koin.dsl.module
import videoTrade.screonPlayer.app.presentation.screens.player.VideoPlayerViewModel

private val domainModule = module {

}

private val presentationModule = module {

    single {
        VideoPlayerViewModel()
    }
}


private fun getAllModules() = listOf(
    domainModule,
    presentationModule
)

fun getSharedModules() = getAllModules()
