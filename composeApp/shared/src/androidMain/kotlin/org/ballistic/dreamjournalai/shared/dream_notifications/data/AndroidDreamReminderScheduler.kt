package org.ballistic.dreamjournalai.shared.dream_notifications.data

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.datetime.LocalTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DreamReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.MaxRealityCheckReminders
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class AndroidDreamReminderScheduler(
    private val context: Context,
) : DreamReminderScheduler {
    override suspend fun setDreamJournalReminderEnabled(enabled: Boolean, reminderTime: LocalTime) {
        if (!enabled) {
            workManager.cancelUniqueWork(DREAM_JOURNAL_WORK_NAME)
            return
        }

        enqueueDailyReminder(
            workName = DREAM_JOURNAL_WORK_NAME,
            reminderTime = reminderTime,
            title = "Write in your dream journal",
            message = "Take a minute to save what you remember.",
            reminderKind = GenericReminderNotificationWorker.KIND_DREAM_JOURNAL
        )
    }

    override suspend fun setRealityCheckReminders(enabled: Boolean, reminderTimes: List<LocalTime>) {
        repeat(MaxRealityCheckReminders) { index ->
            workManager.cancelUniqueWork(realityWorkName(index))
        }

        if (!enabled) return

        reminderTimes
            .take(MaxRealityCheckReminders)
            .forEachIndexed { index, reminderTime ->
                enqueueDailyReminder(
                    workName = realityWorkName(index),
                    reminderTime = reminderTime,
                    title = "Reality check",
                    message = "Pause and ask: am I dreaming?",
                    reminderKind = GenericReminderNotificationWorker.KIND_REALITY_CHECK
                )
            }
    }

    private fun enqueueDailyReminder(
        workName: String,
        reminderTime: LocalTime,
        title: String,
        message: String,
        reminderKind: String,
    ) {
        val data = Data.Builder()
            .putString(GenericReminderNotificationWorker.KEY_TITLE, title)
            .putString(GenericReminderNotificationWorker.KEY_MESSAGE, message)
            .putString(GenericReminderNotificationWorker.KEY_KIND, reminderKind)
            .build()
        val request = PeriodicWorkRequestBuilder<GenericReminderNotificationWorker>(
            1,
            TimeUnit.DAYS
        )
            .setInputData(data)
            .setInitialDelay(initialDelayMillis(reminderTime), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    private fun initialDelayMillis(reminderTime: LocalTime): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var next = LocalDate.now(zone).atTime(reminderTime.hour, reminderTime.minute)

        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }

        return Duration.between(now, next).toMillis().coerceAtLeast(1_000L)
    }

    private fun realityWorkName(index: Int): String = "$REALITY_CHECK_WORK_PREFIX$index"

    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    companion object {
        private const val DREAM_JOURNAL_WORK_NAME = "dream_journal_daily_reminder"
        private const val REALITY_CHECK_WORK_PREFIX = "reality_check_daily_reminder_"
    }
}
