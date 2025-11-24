package videoTrade.screonPlayer.app.domain.model.windgets

enum class WidgetType {
    CLOCK,
    COUNTDOWN,
    WEATHER,
}

enum class WidgetPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER,
    TOP,
    BOTTOM,
}

data class WidgetLayout(
    val position: WidgetPosition,
    val marginDp: Int = 8,
    val widthFraction: Float? = null,
    val heightFraction: Float? = null
)


data class WidgetDescriptor(
    val id: String,
    val type: WidgetType,
    val layout: WidgetLayout,
    val payload: Map<String, String> = emptyMap()
)