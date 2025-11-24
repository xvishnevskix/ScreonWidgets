/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import videoTrade.screonPlayer.app.domain.repository.Connectivity


 object NetworkStatusMonitor {
    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private var initialized = false

    fun start(context: Context) {
        if (initialized) return
        initialized = true

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun computeOnline21to22(): Boolean {
            @Suppress("DEPRECATION")
            val info = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            return info?.isConnected == true
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun computeOnline23plus(): Boolean {
            val active = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(active) ?: return false
            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val validated = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // VALIDATED появился в 23
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } else true
            return hasInternet && validated
        }

        fun computeOnline(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                computeOnline23plus()
            } else {
                computeOnline21to22()
            }
        }

        // первичная установка
        _isOnline.value = computeOnline()

        val req = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val now = computeOnline()
                if (_isOnline.value != now) _isOnline.value = now
                println("Network available, online=$now")
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                val now = computeOnline()
                if (_isOnline.value != now) _isOnline.value = now
                println("Network caps changed, online=$now")
            }

            override fun onLost(network: Network) {
                val now = computeOnline()
                if (_isOnline.value != now) _isOnline.value = now
                println("Network lost, online=$now")
            }
        }

        try {
            cm.registerNetworkCallback(req, cb)
        } catch (t: Throwable) {
            println("registerNetworkCallback failed; falling back to single check")
            _isOnline.value = computeOnline()
        }
    }
}

class AndroidConnectivity(
    context: Context
) : Connectivity {
    override val isOnline = NetworkStatusMonitor.isOnline
    init {
        NetworkStatusMonitor.start(context.applicationContext)
    }
}