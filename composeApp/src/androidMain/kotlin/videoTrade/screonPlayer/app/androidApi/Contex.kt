/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.androidApi

import android.content.Context

object ContextObj {
    
    private lateinit var context: Context
    
    fun getContext(): Context {
        return context
    }
    
    fun setContext(contextNew: Context) {
        context = contextNew.applicationContext
    }
}
