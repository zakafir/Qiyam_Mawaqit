package com.zakafir.scheduler_receiver

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.zakafir.data.core.util.isOreoMr1Plus
import com.zakafir.domain.model.Alarm
import com.zakafir.domain.model.AlarmConstants
import com.zakafir.presentation.ui.theme.Qiyam_MawaqitTheme
import com.zakafir.scheduler_receiver.screen.AlarmTriggerScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class ReminderActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        showOverLockscreen()
        val alarmId = intent?.getStringExtra(AlarmConstants.EXTRA_ALARM_ID) ?: throw Exception("Alarm ID is not found.")

        setContent {
            Qiyam_MawaqitTheme {
                val viewModel: ReminderViewModel = koinViewModel { parametersOf(alarmId) }
                var alarm by remember { mutableStateOf<Alarm?>(null) }
                val lifecycleOwner = LocalLifecycleOwner.current

                LaunchedEffect(viewModel.events) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        withContext(Dispatchers.Main.immediate) {
                            viewModel.events.collect { currentEvent ->
                                when (currentEvent) {
                                    is ReminderEvent.OnAlarmFetched -> {
                                        alarm = currentEvent.alarm
                                    }
                                    else -> finish()
                                }
                            }
                        }
                    }
                }

                alarm?.let {
                    AlarmTriggerScreen(
                        alarm = it,
                        onTurnOffClick = {
                            viewModel.disableOrRescheduleAlarm()
                            finish()
                        },
                        onSnoozeClick = {
                            viewModel.snoozeAlarm()
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun showOverLockscreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (isOreoMr1Plus()) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }
}