package com.zakafir.presentation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.zakafir.data.core.util.isTiramisuPlus
import com.zakafir.presentation.screen.QiyamApp
import com.zakafir.presentation.ui.theme.Qiyam_MawaqitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (isTiramisuPlus()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        setContent {
            Qiyam_MawaqitTheme {
                QiyamApp()
            }
        }
    }
}