package org.ballistic.dreamjournalai.shared.dream_notifications.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DailyTokenReminderScheduler
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AndroidDailyTokenReminderScheduler(
    private val context: Context
) : DailyTokenReminderScheduler {
    override suspend fun setDailyTokenReminderEnabled(enabled: Boolean, reminderTime: LocalTime) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putInt(KEY_HOUR, reminderTime.hour)
            .putInt(KEY_MINUTE, reminderTime.minute)
            .apply()

        if (enabled) {
            scheduleNext(reminderTime)
        } else {
            cancel()
        }
    }

    override suspend fun syncDailyTokenReminder(hasFullyClaimedToday: Boolean, reminderTime: LocalTime) {
        val editor = prefs.edit()
            .putInt(KEY_HOUR, reminderTime.hour)
            .putInt(KEY_MINUTE, reminderTime.minute)

        if (hasFullyClaimedToday) {
            editor.putString(KEY_LAST_CLAIM_DAY, currentLocalDay())
        } else {
            editor.remove(KEY_LAST_CLAIM_DAY)
        }
        editor.apply()

        if (prefs.getBoolean(KEY_ENABLED, true)) {
            scheduleNext(reminderTime)
        }
    }

    internal fun scheduleNext(reminderTime: LocalTime = storedReminderTime()) {
        val request = OneTimeWorkRequestBuilder<DailyTokenReminderWorker>()
            .setInitialDelay(nextDelayMillis(reminderTime), TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    DailyTokenReminderWorker.KEY_TITLE to "Claim your daily token",
                    DailyTokenReminderWorker.KEY_MESSAGE to "Your DreamNorth token is ready."
                )
            )
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    internal fun shouldShowReminderToday(): Boolean {
        return prefs.getBoolean(KEY_ENABLED, true) &&
            prefs.getString(KEY_LAST_CLAIM_DAY, null) != currentLocalDay()
    }

    private fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun storedReminderTime(): LocalTime {
        return LocalTime(
            hour = prefs.getInt(KEY_HOUR, 8).coerceIn(0, 23),
            minute = prefs.getInt(KEY_MINUTE, 0).coerceIn(0, 59)
        )
    }

    private fun nextDelayMillis(reminderTime: LocalTime): Long {
        val timeZone = TimeZone.currentSystemDefault()
        val nowInstant = Clock.System.now()
        val now = nowInstant.toLocalDateTime(timeZone)
        var next = LocalDateTime(
            year = now.year,
            month = now.month,
            dayOfMonth = now.dayOfMonth,
            hour = reminderTime.hour,
            minute = reminderTime.minute,
            second = 0,
            nanosecond = 0
        )

        if (next <= now || prefs.getString(KEY_LAST_CLAIM_DAY, null) == currentLocalDay()) {
            val tomorrow = now.date.plus(DatePeriod(days = 1))
            next = LocalDateTime(
                year = tomorrow.year,
                month = tomorrow.month,
                dayOfMonth = tomorrow.dayOfMonth,
                hour = reminderTime.hour,
                minute = reminderTime.minute,
                second = 0,
                nanosecond = 0
            )
        }

        return max(1_000L, next.toInstant(timeZone).toEpochMilliseconds() - nowInstant.toEpochMilliseconds())
    }

    private fun currentLocalDay(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }

    private val prefs
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        internal const val WORK_NAME = "daily_token_reminder"
        internal const val WORK_TAG = "daily_token_reminder"
        private const val PREFS_NAME = "daily_token_reminder_preferences"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_HOUR = "hour"
        private const val KEY_MINUTE = "minute"
        private const val KEY_LAST_CLAIM_DAY = "last_claim_day"
    }
}
