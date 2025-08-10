package com.zakafir.qiyam_mawaqit.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zakafir.qiyam_mawaqit.data.PrayerTimesClient
import com.zakafir.qiyam_mawaqit.domain.PrayerTimesRepository
import com.zakafir.qiyam_mawaqit.domain.PrayerTimesRepositoryImpl
import com.zakafir.qiyam_mawaqit.presentation.HistoryScreen
import com.zakafir.qiyam_mawaqit.presentation.PrayerTimesViewModel
import com.zakafir.qiyam_mawaqit.presentation.QiyamUiState
import com.zakafir.qiyam_mawaqit.presentation.Screen
import com.zakafir.qiyam_mawaqit.presentation.demoState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime


@Composable
fun QiyamApp(
    state: QiyamUiState,
    onScheduleTonight: (LocalDateTime) -> Unit = {},
    onMarkWoke: (LocalDate) -> Unit = {},
    onMarkPrayed: (LocalDate) -> Unit = {},
    onUpdateBuffer: (Int) -> Unit = {},
    onUpdateWeeklyGoal: (Int) -> Unit = {}
) {
    val nav = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavBar(nav) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            val viewModel = PrayerTimesViewModel(
                repo = PrayerTimesRepositoryImpl(
                    api = PrayerTimesClient()
                )
            )
            viewModel.refresh()
            composable(Screen.Home.route) {
                HomeScreen(
                    state = state,
                    onSchedule = { onScheduleTonight(it); },
                    onTestAlarmUi = { nav.navigate(Screen.Wake.route) },
                    onOpenHistory = { nav.navigate(Screen.History.route) },
                    onOpenSettings = { nav.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Wake.route) {
                WakeScreen(
                    time = state.window.suggestedWake,
                    onImUp = { onMarkWoke(state.today); nav.popBackStack() },
                    onMarkPrayed = { onMarkPrayed(state.today); nav.popBackStack() },
                    onSnooze = { /* no-op in UI-only demo */ }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(state.history)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    bufferMinutes = state.bufferMinutes,
                    weeklyGoal = state.weeklyGoal,
                    onBufferChange = onUpdateBuffer,
                    onGoalChange = onUpdateWeeklyGoal
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(nav: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute(nav) == Screen.Home.route,
            onClick = { nav.navigate(Screen.Home.route) },
            icon = { Icon(Icons.Default.Phone, null) },
            label = { Text("Tonight") }
        )
        NavigationBarItem(
            selected = currentRoute(nav) == Screen.History.route,
            onClick = { nav.navigate(Screen.History.route) },
            icon = { Icon(Icons.Default.Build, null) },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = currentRoute(nav) == Screen.Settings.route,
            onClick = { nav.navigate(Screen.Settings.route) },
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text("Settings") }
        )
    }
}

@Composable
private fun currentRoute(nav: NavHostController): String? {
    val backStack by nav.currentBackStackEntryFlow.collectAsState(initial = nav.currentBackStackEntry)
    return backStack?.destination?.route
}


@Preview(showBackground = true)
@Composable
private fun PreviewHome() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        QiyamApp(demoState())
    }
}