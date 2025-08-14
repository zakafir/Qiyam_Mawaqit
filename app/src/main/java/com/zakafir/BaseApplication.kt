package com.zakafir

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.zakafir.data.core.util.isOreoPlus
import com.zakafir.data.DataModule
import com.zakafir.data.core.database.di.coreDatabaseModule
import com.zakafir.data.core.database.di.featureAlarmDataModule
import com.zakafir.data.core.ringtone.di.coreRingtoneModule
import com.zakafir.domain.model.AlarmConstants
import com.zakafir.presentation.PresentationModule
import com.zakafir.presentation.di.featureAlarmPresentationModule
import com.zakafir.scheduler_receiver.di.appModule
import com.zakafir.scheduler_receiver.di.featureAlarmSchedulerReceiverModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.waitKoinStart
import org.koin.core.lazyModules
import org.koin.dsl.lazyModule

class BaseApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val modules = listOf(
            DataModule.getModule(),
            PresentationModule.getModule(),
            appModule,
            coreDatabaseModule,
            coreRingtoneModule,
            featureAlarmDataModule,
            featureAlarmSchedulerReceiverModule,
            featureAlarmPresentationModule
        )
        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            lazyModules(modules)
        }
        waitKoinStart()
    }

    private fun createNotificationChannel() {
        if (isOreoPlus()) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                AlarmConstants.CHANNEL_ID, "Alarm", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setBypassDnd(true)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

