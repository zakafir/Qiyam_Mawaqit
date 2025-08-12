package com.zakafir.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zakafir.presentation.screen.QiyamApp
import com.zakafir.presentation.ui.theme.Qiyam_MawaqitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Qiyam_MawaqitTheme {
                QiyamApp()
            }
        }
    }
}