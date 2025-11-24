/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import io.ktor.client.HttpClient

object HttpClientProvider {
    val client: HttpClient by lazy {
        HttpClient {
            expectSuccess = false
            // Добавь timeout или engine-конфиг по желанию
        }
    }
}
