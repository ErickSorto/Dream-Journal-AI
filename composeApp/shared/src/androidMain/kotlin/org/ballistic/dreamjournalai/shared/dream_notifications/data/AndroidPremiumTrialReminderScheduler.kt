package org.ballistic.dreamjournalai.shared.dream_notifications.data

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.PremiumTrialReminderScheduler
import java.util.concurrent.TimeUnit

class AndroidPremiumTrialReminderScheduler(
    private val context: Context,
) : PremiumTrialReminderScheduler {
    override suspend fun scheduleTrialEndingReminder(triggerAtEpochMillis: Long) {
        val delayMillis = (triggerAtEpochMillis - System.currentTimeMillis()).coerceAtLeast(1_000L)
        val data = Data.Builder()
            .putString(GenericReminderNotificationWorker.KEY_TITLE, "Your free trial ends soon")
            .putString(
                GenericReminderNotificationWorker.KEY_MESSAGE,
                "DreamNorth Premium renews in 2 days. You can manage or cancel anytime."
            )
            .putString(GenericReminderNotificationWorker.KEY_KIND, GenericReminderNotificationWorker.KIND_PREMIUM_TRIAL)
            .build()
        val request = OneTimeWorkRequestBuilder<GenericReminderNotificationWorker>()
            .setInputData(data)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override suspend fun cancelTrialEndingReminder() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    companion object {
        private const val WORK_NAME = "premium_trial_ending_reminder"
    }
}
