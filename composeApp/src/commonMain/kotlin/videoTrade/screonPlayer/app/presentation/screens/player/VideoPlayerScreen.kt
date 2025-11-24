/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.presentation.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import screonplayer.composeapp.generated.resources.Logo
import screonplayer.composeapp.generated.resources.Res
import videoTrade.screonPlayer.app.domain.model.windgets.WidgetDescriptor
import videoTrade.screonPlayer.app.domain.model.windgets.WidgetLayout
import videoTrade.screonPlayer.app.domain.model.windgets.WidgetPosition
import videoTrade.screonPlayer.app.domain.model.windgets.WidgetType
import videoTrade.screonPlayer.app.domain.repository.VideoPlayer
import videoTrade.screonPlayer.app.presentation.components.widgets.WidgetsOverlay
import kotlin.math.min


class VideoPlayerScreen(
) : Screen, KoinComponent {
    @Composable
    override fun Content() {
        val player: VideoPlayer by inject()
        val viewModel: VideoPlayerViewModel = koinInject()
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Box(Modifier.fillMaxSize()) {

                        RotatedOverlay(rotationDeg = 0) {


                            DemoBadge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .fillMaxWidth()
                            )


                            val widgets = remember {
                                listOf(
                                    WidgetDescriptor(
                                        id = "weather-1",
                                        type = WidgetType.WEATHER,
                                        layout = WidgetLayout(
                                            position = WidgetPosition.BOTTOM_RIGHT,
                                            marginDp = 16,
                                            widthFraction = 0.2f,
                                        ),
                                        payload = mapOf(
                                            "city" to "Moscow",
                                            "lat" to "55.75",
                                            "lon" to "37.61"
                                        )
                                    )
                                    // CLOCK, COUNTDOWN и др.
                                )
                            }

                            WidgetsOverlay(
                                widgets = widgets,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
            }
        }
    }
}


@Composable
fun DemoBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(12.dp)
            .zIndex(10f)
            .wrapContentSize(Alignment.TopEnd)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xCC000000))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.Logo),
                contentDescription = null,
                modifier = Modifier.height(40.dp).width(150.dp).padding(start = 6.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Демоверсия",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 2.dp)
            )
        }
    }
}


@Composable
private fun RotatedOverlay(
    rotationDeg: Int,
    content: @Composable BoxScope.() -> Unit
) {
    var parent by remember { mutableStateOf(IntSize.Zero) }

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { parent = it }
            .graphicsLayer {
                val rot = ((rotationDeg % 360) + 360) % 360
                rotationZ = rot.toFloat()
                transformOrigin = TransformOrigin(0f, 0f)

                val w = parent.width.toFloat().coerceAtLeast(1f)
                val h = parent.height.toFloat().coerceAtLeast(1f)

                val r = w / h
                val needContain = rot % 180 != 0
                val s = if (needContain) min(r, 1f / r) else 1f
                scaleX = s
                scaleY = s

                translationX = 0f
                translationY = 0f
                when (rot) {
                    90  -> translationX = h * s
                    180 -> { translationX = w * s; translationY = h * s }
                    270 -> translationY = w * s
                }

                clip = false
            }
            .zIndex(5f)
    ) {
        content()
    }
}