package videoTrade.screonPlayer.app.multiplatform

import android.content.Context

internal fun readAssetText(context: Context, assetPath: String): String =
    runCatching {
        context.assets.open(assetPath).bufferedReader().use { it.readText() }
    }.getOrElse { "⚠️ Не удалось открыть: $assetPath\n\n${it.message.orEmpty()}" }