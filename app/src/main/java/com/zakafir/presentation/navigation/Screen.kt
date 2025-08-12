package com.zakafir.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Wake : Screen("wake")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}