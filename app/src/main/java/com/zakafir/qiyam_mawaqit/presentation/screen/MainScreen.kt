package com.zakafir.qiyam_mawaqit.presentation.screen

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

import com.zakafir.qiyam_mawaqit.presentation.navigation.Screen
import com.zakafir.qiyam_mawaqit.presentation.screen.HistoryScreen
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zakafir.qiyam_mawaqit.domain.PrayerTimesRepositoryImpl
import com.zakafir.qiyam_mawaqit.presentation.PrayerTimesViewModel
import com.zakafir.qiyam_mawaqit.presentation.QiyamLog
import com.zakafir.qiyam_mawaqit.presentation.QiyamUiState
import com.zakafir.qiyam_mawaqit.presentation.demoState
import com.zakafir.qiyam_mawaqit.presentation.screen.NapConfig


@Composable
fun QiyamApp(
    onScheduleTonight: (LocalDateTime) -> Unit = {},
    onMarkWoke: (LocalDate) -> Unit = {},
    onMarkPrayed: (LocalDate) -> Unit = {},
    onUpdateBuffer: (Int) -> Unit = {},
    onUpdateWeeklyGoal: (Int) -> Unit = {}
) {
    val nav = rememberNavController()
    val context = LocalContext.current
    val viewModel = remember {
        PrayerTimesViewModel(
            repo = PrayerTimesRepositoryImpl()
        )
    }
    Scaffold(
        bottomBar = { BottomNavBar(nav) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                LaunchedEffect(Unit) {
                    viewModel.refresh(context)
                }
                val vmState = viewModel.uiState.collectAsState().value
                HomeScreen(
                    vmUiState = vmState,
                    onSchedule = { onScheduleTonight(it) },
                    onTestAlarmUi = { nav.navigate(Screen.Wake.route) },
                    onOpenHistory = { nav.navigate(Screen.History.route) },
                    onOpenSettings = { nav.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Wake.route) {
                val vmState = viewModel.uiState.collectAsState().value
                val fallbackTime = LocalDateTime(2025, 1, 1, 3, 30)
                val fallbackDate = LocalDate(2025, 1, 1)
                WakeScreen(
                    time = vmState.qiyamUiState?.suggestedWake ?: fallbackTime,
                    onImUp = { onMarkWoke(fallbackDate); nav.popBackStack() },
                    onMarkPrayed = { onMarkPrayed(fallbackDate); nav.popBackStack() },
                    onSnooze = { /* no-op in UI-only demo */ }
                )
            }
            composable(Screen.History.route) {
                val vmState = viewModel.uiState.collectAsState().value
                HistoryScreen(
                    listOf(
                        QiyamLog(date = LocalDate(2025, 1, 1), prayed = true, woke = true),
                        QiyamLog(date = LocalDate(2025, 1, 2), prayed = false, woke = true),
                    )
                )
            }
            composable(Screen.Settings.route) {
                val vmState = viewModel.uiState.collectAsState().value

                SettingsScreen(
                    onBufferChange = { viewModel.updateBuffer(it) },
                    onGoalChange = { viewModel.updateWeeklyGoal(it) },

                    ui = vmState, // if you refactored SettingsScreen to take the PrayerUiState

                    onDesiredSleepHoursChange = { viewModel.updateDesiredSleepHours(it) },
                    onPostFajrBufferMinChange = { viewModel.updatePostFajrBuffer(it) },
                    onIshaBufferMinChange = { viewModel.updateIshaBuffer(it) },
                    onMinNightStartChange = { viewModel.updateMinNightStart(it) },
                    onDisallowPostFajrIfFajrAfterChange = { viewModel.updatePostFajrCutoff(it) },

                    onUpdateNap = { index, config -> viewModel.updateNap(index, config) },
                    onAddNap = { viewModel.addNap() },
                    onRemoveNap = { index -> viewModel.removeNap(index) },
                    onLatestMorningEndChange = { viewModel.updateLatestMorningEnd(it) }
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
        QiyamApp()
    }
}