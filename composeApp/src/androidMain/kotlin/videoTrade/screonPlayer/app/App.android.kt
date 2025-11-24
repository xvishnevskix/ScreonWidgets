/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import videoTrade.screonPlayer.app.androidApi.ContextObj
import videoTrade.screonPlayer.app.di.getAndroidModules
import videoTrade.screonPlayer.app.di.getSharedModules
import videoTrade.screonPlayer.app.multiplatform.NetworkStatusMonitor


class AppActivity : ComponentActivity() {



    private val CAMERA_AUDIO_PERMS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )
    private val REQ_CODE_CAMERA_AUDIO = 1001

    private fun hasAllPermissions(): Boolean =
        CAMERA_AUDIO_PERMS.all { perm ->
            androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                perm
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {

        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        startKoin {
            androidContext(this@AppActivity)
            modules(
                getSharedModules() + getAndroidModules() + listOf(
                )
            )
        }


        ContextObj.setContext(this)

        NetworkStatusMonitor.start(applicationContext)

        if (!hasAllPermissions()) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                CAMERA_AUDIO_PERMS,
                REQ_CODE_CAMERA_AUDIO
            )
        }

        requestLocationPermissionsIfNeeded()

        setContent {
            App()
        }
    }

    private fun requestLocationPermissionsIfNeeded() {
        val needFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        val needCoarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        if (needFine || needCoarse) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
        }
    }

}


