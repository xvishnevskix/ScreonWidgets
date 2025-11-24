/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.di

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.videolan.libvlc.util.VLCVideoLayout
import videoTrade.screonPlayer.app.domain.repository.VideoPlayer



fun getAndroidModules() = listOf(
    module {


        single<@Composable () -> Unit> {

            @Composable {


            }
        }
    }
)