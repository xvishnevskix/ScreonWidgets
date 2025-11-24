package videoTrade.screonPlayer.app.presentation.components.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import videoTrade.screonPlayer.app.domain.model.windgets.WidgetDescriptor
import videoTrade.screonPlayer.app.domain.model.windgets.WidgetPosition
import videoTrade.screonPlayer.app.domain.model.windgets.WidgetType

@Composable
fun WidgetsOverlay(
    widgets: List<WidgetDescriptor>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        widgets.forEach { widget ->
            val baseModifier = Modifier
                .padding(widget.layout.marginDp.dp)
                .then(
                    when {
                        widget.layout.widthFraction != null &&
                                widget.layout.heightFraction != null ->
                            Modifier
                                .fillMaxWidth(widget.layout.widthFraction)
                                .fillMaxHeight(widget.layout.heightFraction)

                        widget.layout.widthFraction != null ->
                            Modifier.fillMaxWidth(widget.layout.widthFraction)

                        widget.layout.heightFraction != null ->
                            Modifier.fillMaxHeight(widget.layout.heightFraction)

                        else -> Modifier
                    }
                )

            val alignedModifier = when (widget.layout.position) {
                WidgetPosition.TOP_LEFT     -> baseModifier.align(Alignment.TopStart)
                WidgetPosition.TOP_RIGHT    -> baseModifier.align(Alignment.TopEnd)
                WidgetPosition.BOTTOM_LEFT  -> baseModifier.align(Alignment.BottomStart)
                WidgetPosition.BOTTOM_RIGHT -> baseModifier.align(Alignment.BottomEnd)
                WidgetPosition.CENTER       -> baseModifier.align(Alignment.Center)
                WidgetPosition.TOP -> baseModifier.align(Alignment.TopCenter).fillMaxWidth()
                WidgetPosition.BOTTOM -> baseModifier.align(Alignment.BottomCenter).fillMaxWidth()
            }


            when (widget.type) {
                WidgetType.WEATHER   -> WeatherWidget(widget, alignedModifier)
                // дальше добавлять остальные
                else -> {}
            }
        }
    }
}
