/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.domain.repository

import kotlinx.coroutines.flow.StateFlow


interface Connectivity {
    val isOnline: StateFlow<Boolean>
}