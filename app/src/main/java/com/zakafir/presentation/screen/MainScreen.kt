package com.zakafir.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.datetime.LocalDateTime
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.zakafir.presentation.PrayerTimesViewModel
import com.zakafir.presentation.navigation.Screen


@Composable
fun QiyamApp(
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
                    onTestAlarmUi = { nav.navigate(Screen.Wake.route) },
                    onMasjidIdChange = { sharedViewModel.updateMasjidId(it) },
                    onSelectMasjidSuggestion = { sharedViewModel.selectMasjidSuggestion(it) },
                    onComputeQiyam = { today, tomorrow ->
                        sharedViewModel.computeQiyamWindow(today, tomorrow)
                    },
                    onModeChange = { sharedViewModel.updateQiyamMode(it) },
                    onOpenDetailsScreen = { selectedMosque ->
                        nav.navigate(Screen.Details.route)
                    },
                    onLogPrayed = { prayed -> sharedViewModel.logQiyamForToday(prayed) },
                )
            }
            composable(Screen.Wake.route) {
                val vmState = sharedViewModel.uiState.collectAsState().value
                val fallbackTime = LocalDateTime(2025, 1, 1, 3, 30)
                WakeScreen(
                    time = vmState.qiyamUiState?.suggestedWake ?: fallbackTime,
                    onSnooze = { /* no-op in UI-only demo */ }
                )
            }
            composable(Screen.Details.route) {
                val vmState = sharedViewModel.uiState.collectAsState().value

                vmState.selectedMosque?.let { mosque ->
                    DetailsScreen(
                        mosque = mosque,
                        onCancel = {
                            sharedViewModel.resetData()
                            nav.popBackStack()
                        },
                        onConfirm = {
                            sharedViewModel.refresh()
                            nav.popBackStack()
                        }
                    )
                }
            }
            composable(Screen.History.route) {
                val vmState = sharedViewModel.uiState.collectAsState().value
                LaunchedEffect(Unit) {
                    sharedViewModel.loadQiyamHistory()
                }
                vmState.qiyamUiState?.qiyamHistory?.let { history ->
                    HistoryScreen(
                        streak = vmState.streak,
                        weeklyGoal = vmState.weeklyGoal,
                        isRefreshing = vmState.isRefreshing,
                        history = history,
                        onRefresh = { sharedViewModel.onPullToRefresh() },
                        onUpdateLog = { date, prayed ->
                            sharedViewModel.updateQiyamLog(date, prayed)
                        }
                    )
                }
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

    data class Tab(val route: String, val icon: ImageVector, val label: String)

    val tabs = listOf(
        Tab(Screen.Home.route, Icons.Default.AccountBox, "Tonight"),
        Tab(Screen.History.route, Icons.Default.DateRange, "History"),
        Tab(Screen.Settings.route, Icons.Default.Settings, "Settings"),
    )

    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, null) },
                label = { Text(tab.label) }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewHome() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        QiyamApp()
    }
}