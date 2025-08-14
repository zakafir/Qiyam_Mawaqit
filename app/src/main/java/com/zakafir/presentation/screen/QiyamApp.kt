package com.zakafir.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.toRoute
import com.zakafir.data.core.domain.ringtone.NameAndUri
import com.zakafir.presentation.PrayerTimesViewModel
import com.zakafir.presentation.QiyamAlarm
import com.zakafir.presentation.add_edit.AddEditAlarmAction
import com.zakafir.presentation.add_edit.AddEditAlarmScreenRoot
import com.zakafir.presentation.add_edit.AddEditAlarmViewModel
import com.zakafir.presentation.list.AlarmListScreenRoot
import com.zakafir.presentation.navigation.RootGraph
import com.zakafir.presentation.ringtone_list.RingtoneListScreenRoot
import com.zakafir.presentation.ringtone_list.RingtoneListViewModel
import com.zakafir.presentation.util.composableWithTransitions
import org.koin.core.parameter.parametersOf

@Composable
fun QiyamApp(
) {
    val navController = rememberNavController()
    val sharedViewModel: PrayerTimesViewModel = koinViewModel()
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RootGraph.Home,
            modifier = Modifier.padding(padding),
        ) {
            composableWithTransitions<RootGraph.Home> {
                val vmState = sharedViewModel.uiState.collectAsState().value
                HomeScreen(
                    vmUiState = vmState,
                    onAddQiyamAlarm = {
                        navController.navigate(
                            RootGraph.AlarmDetail(
                                alarmId = null,
                                alarmHour = it.hour,
                                alarmMinute = it.minute,
                                alarmName = it.alarmName
                            )
                        )
                    },
                    onMasjidIdChange = { sharedViewModel.updateMasjidId(it) },
                    onSelectMasjidSuggestion = { sharedViewModel.selectMasjidSuggestion(it) },
                    onComputeQiyam = { today, tomorrow ->
                        sharedViewModel.computeQiyamWindow(today, tomorrow)
                    },
                    onModeChange = { sharedViewModel.updateQiyamMode(it) },
                    onOpenDetailsScreen = { selectedMosque ->
                        navController.navigate(RootGraph.Details)
                    },
                    onLogPrayed = { prayed -> sharedViewModel.logQiyamForToday(prayed) },
                )
            }
            composableWithTransitions<RootGraph.Details> {
                val vmState = sharedViewModel.uiState.collectAsState().value

                vmState.selectedMosque?.let { mosque ->
                    DetailsScreen(
                        mosque = mosque,
                        onCancel = {
                            sharedViewModel.resetData()
                            navController.popBackStack()
                        },
                        onConfirm = {
                            sharedViewModel.refresh()
                            navController.popBackStack()
                        }
                    )
                }
            }
            composableWithTransitions<RootGraph.History> {
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
            composableWithTransitions<RootGraph.Settings> {
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
            composableWithTransitions<RootGraph.AlarmList> {
                AlarmListScreenRoot(
                    navigateToAddEditScreen = {
                        navController.navigate(RootGraph.AlarmDetail(alarmId = it))
                    }
                )
            }

            composableWithTransitions<RootGraph.AlarmDetail> { entry ->
                val alarmDetailRoute: RootGraph.AlarmDetail = entry.toRoute()
                val viewModel: AddEditAlarmViewModel =
                    koinViewModel { parametersOf(alarmDetailRoute.alarmId) }
                viewModel.onAction(
                    AddEditAlarmAction.OnSetAlarmForQiyam(
                        QiyamAlarm(
                            hour = alarmDetailRoute.alarmHour,
                            minute = alarmDetailRoute.alarmMinute,
                            alarmName = alarmDetailRoute.alarmName
                        )
                    )
                )
                LaunchedEffect(Unit) {
                    val nameAndUri = entry.savedStateHandle.get<NameAndUri>("selectedRingtone")
                        ?: return@LaunchedEffect
                    viewModel.onAction(AddEditAlarmAction.OnAlarmRingtoneChange(nameAndUri))
                }

                AddEditAlarmScreenRoot(
                    navigateBack = {
                        navController.navigateUp()
                    },
                    navigateToRingtoneList = {
                        val (name, uri) = viewModel.state.ringtone ?: Pair(null, null)
                        navController.navigate(RootGraph.RingtoneList(name, uri))
                    },
                    viewModel = viewModel
                )
            }

            composableWithTransitions<RootGraph.RingtoneList> { entry ->
                val ringtoneListRoute: RootGraph.RingtoneList = entry.toRoute()
                val viewModel: RingtoneListViewModel =
                    koinViewModel { parametersOf(ringtoneListRoute.getNameAndUri()) }

                RingtoneListScreenRoot(
                    onRingtoneSelected = {
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "selectedRingtone",
                            it
                        )
                    },
                    navigateBack = {
                        navController.navigateUp()
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(nav: NavHostController) {
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    // Parse the current destination into our sealed route type (may be null for unknown routes)
    val currentRoute: RootGraph? = try {
        navBackStackEntry?.toRoute<RootGraph>()
    } catch (_: Exception) {
        null
    }

    data class Tab(val route: RootGraph, val icon: ImageVector, val label: String)

    val tabs = listOf(
        Tab(RootGraph.Home, Icons.Default.AccountBox, "Tonight"),
        Tab(RootGraph.History, Icons.Default.DateRange, "History"),
        Tab(RootGraph.AlarmList, Icons.Default.Notifications, "Alarms"),
        Tab(RootGraph.Settings, Icons.Default.Settings, "Settings"),
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