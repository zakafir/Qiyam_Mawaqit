package com.zakafir.qiyam_mawaqit.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import java.time.ZonedDateTime

/*
fun scheduleAndroidAlarms(ctx: Context, wake: ZonedDateTime, preCueMinutes: Int = 10) {
    val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val finalIntent = PendingIntent.getBroadcast(
        ctx, 1001, Intent(ctx, FinalAlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmMgr.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, wake.toInstant().toEpochMilli(), finalIntent
    )

    val preCue = wake.minusMinutes(preCueMinutes)
    WorkManager.getInstance(ctx).enqueueUniqueWork(
        "qiyam_precue_${wake.toLocalDate()}",
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<PreCueWorker>()
            .setInitialDelay(Duration.between(Instant.now(), preCue.toInstant()))
            .build()
    )
}*/
