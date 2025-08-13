package com.zakafir.presentation.screen

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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.zakafir.presentation.PrayerTimesViewModel
import com.zakafir.presentation.QiyamLog
import com.zakafir.presentation.navigation.Screen


@Composable
fun QiyamApp(
    onScheduleTonight: (LocalDateTime) -> Unit = {},
    onMarkWoke: (LocalDate) -> Unit = {},
    onMarkPrayed: (LocalDate) -> Unit = {},
    onUpdateBuffer: (Int) -> Unit = {},
    onUpdateWeeklyGoal: (Int) -> Unit = {}
) {
    val nav = rememberNavController()
    val sharedViewModel: PrayerTimesViewModel = koinViewModel()
    val context = LocalContext.current
    Scaffold(
        bottomBar = { BottomNavBar(nav) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                val vmState = sharedViewModel.uiState.collectAsState().value
                HomeScreen(
                    vmUiState = vmState,
                    onSchedule = { onScheduleTonight(it) },
                    onTestAlarmUi = { nav.navigate(Screen.Wake.route) },
                    onOpenSettings = { nav.navigate(Screen.Settings.route) },
                    onMasjidIdChange = { sharedViewModel.updateMasjidId(it) },
                    onSelectMasjidSuggestion = { sharedViewModel.selectMasjidSuggestion(it) },
                    onComputeQiyam = { today, tomorrow ->
                        sharedViewModel.computeQiyamWindow(today, tomorrow)
                    },
                    onModeChange = { sharedViewModel.updateQiyamMode(it) }
                )
            }
            composable(Screen.Wake.route) {
                val vmState = sharedViewModel.uiState.collectAsState().value
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
                val vmState = sharedViewModel.uiState.collectAsState().value
                HistoryScreen(
                    listOf(
                        QiyamLog(date = LocalDate(2025, 1, 1), prayed = true, woke = true),
                        QiyamLog(date = LocalDate(2025, 1, 2), prayed = false, woke = true),
                    )
                )
            }
            composable(Screen.Settings.route) {
                val vmState = sharedViewModel.uiState.collectAsState().value

                SettingsScreen(
                    ui = vmState, // if you refactored SettingsScreen to take the PrayerUiState

                    onDesiredSleepHoursChange = { sharedViewModel.updateDesiredSleepHours(it) },
                    onPostFajrBufferMinChange = { sharedViewModel.updatePostFajrBuffer(it) },
                    onIshaBufferMinChange = { sharedViewModel.updateIshaBuffer(it) },
                    onMinNightStartChange = { sharedViewModel.updateMinNightStart(it) },
                    onDisallowPostFajrIfFajrAfterChange = { sharedViewModel.updatePostFajrCutoff(it) },

                    onUpdateNap = { index, config -> sharedViewModel.updateNap(index, config) },
                    onAddNap = { sharedViewModel.addNap() },
                    onRemoveNap = { index -> sharedViewModel.removeNap(index) },
                    onLatestMorningEndChange = { sharedViewModel.updateLatestMorningEnd(it) },
                    onEnableNapsChange = {
                        sharedViewModel.enableNaps(it)
                    },
                    onEnablePostFajrChange = {
                        sharedViewModel.enablePostFajr(it)
                    },
                    onEnableIshaBufferChange = {
                        sharedViewModel.enableIshaBuffer(it)
                    },
                    onWorkStartChange = { sharedViewModel.updateWorkStart(it) },
                    onWorkEndChange = { sharedViewModel.updateWorkEnd(it) },
                    onCommuteToMinChange = { sharedViewModel.updateCommuteToMin(it) },
                    onCommuteFromMinChange = {
                        sharedViewModel.updateCommuteFromMin(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(nav: NavHostController) {
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {
                nav.navigate(Screen.Home.route) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Phone, null) },
            label = { Text("Tonight") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.History.route,
            onClick = {
                nav.navigate(Screen.History.route) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Build, null) },
            label = { Text("History") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                nav.navigate(Screen.Settings.route) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text("Settings") }
        )
    }
}



@Preview(showBackground = true)
@Composable
private fun PreviewHome() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        QiyamApp()
    }
}