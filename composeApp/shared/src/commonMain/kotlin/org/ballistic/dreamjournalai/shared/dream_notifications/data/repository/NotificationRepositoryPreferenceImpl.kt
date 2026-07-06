package org.ballistic.dreamjournalai.shared.dream_notifications.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DefaultDailyTokenReminderTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DefaultDreamJournalReminderTime
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DefaultRealityCheckReminderTimes
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.FreeRealityCheckReminderLimit
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.MaxRealityCheckReminders
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationSettingsRepository

/**
 * A common implementation that reads/writes from a provided DataStore<Preferences>.
 */
class NotificationSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : NotificationSettingsRepository {

    // region: flows
    override val realityCheckReminderFlow: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[Keys.REALITY_CHECK_REMINDER] ?: false
        }

    override val dreamJournalReminderFlow: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[Keys.DREAM_JOURNAL_REMINDER] ?: false
        }

    override val dailyTokenReminderFlow: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[Keys.DAILY_TOKEN_REMINDER] ?: true
        }

    override val dailyTokenReminderTimeFlow: Flow<LocalTime> =
        dataStore.data.map { prefs ->
            parseLocalTimeOrDefault(
                str = prefs[Keys.DAILY_TOKEN_REMINDER_TIME],
                default = DefaultDailyTokenReminderTime
            )
        }

    override val realityCheckReminderTimesFlow: Flow<List<LocalTime>> =
        dataStore.data.map { prefs ->
            parseRealityCheckTimes(prefs[Keys.REALITY_CHECK_REMINDER_TIMES])
        }

    override val lucidityFrequencyFlow: Flow<Int> =
        dataStore.data.map { prefs ->
            (prefs[Keys.LUCIDITY_FREQUENCY] ?: 1).coerceIn(1, MaxRealityCheckReminders)
        }

    override val reminderTimeFlow: Flow<LocalTime> =
        dataStore.data.map { prefs ->
            val timeStr: String? = prefs[Keys.REMINDER_TIME]
            parseLocalTimeOrDefault(timeStr, DefaultDreamJournalReminderTime)
        }

    override val timeRangeFlow: Flow<ClosedFloatingPointRange<Float>> =
        dataStore.data.map { prefs ->
            val start = prefs[Keys.START_TIME] ?: 0f
            val end = prefs[Keys.END_TIME] ?: 1440f
            start..end
        }
    // endregion flows

    // region: updates
    override suspend fun updateRealityCheckReminder(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.REALITY_CHECK_REMINDER] = value
        }
    }

    override suspend fun updateDreamJournalReminder(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.DREAM_JOURNAL_REMINDER] = value
        }
    }

    override suspend fun updateDailyTokenReminder(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.DAILY_TOKEN_REMINDER] = value
        }
    }

    override suspend fun updateDailyTokenReminderTime(value: LocalTime) {
        dataStore.edit { prefs ->
            prefs[Keys.DAILY_TOKEN_REMINDER_TIME] = value.toString()
        }
    }

    override suspend fun updateRealityCheckReminderTimes(value: List<LocalTime>) {
        dataStore.edit { prefs ->
            prefs[Keys.REALITY_CHECK_REMINDER_TIMES] = value
                .take(MaxRealityCheckReminders)
                .joinToString(separator = ",") { it.toString() }
        }
    }

    override suspend fun updateLucidityFrequency(value: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.LUCIDITY_FREQUENCY] = value.coerceIn(1, MaxRealityCheckReminders)
        }
    }

    override suspend fun updateReminderTime(value: LocalTime) {
        dataStore.edit { prefs ->
            prefs[Keys.REMINDER_TIME] = value.toString() // e.g. "07:00"
        }
    }

    override suspend fun updateTimeRange(start: Float, end: Float) {
        dataStore.edit { prefs ->
            prefs[Keys.START_TIME] = start
            prefs[Keys.END_TIME] = end
        }
    }
    // endregion updates

    // region: keys
    private object Keys {
        val REALITY_CHECK_REMINDER = booleanPreferencesKey("reality_check_reminder")
        val DREAM_JOURNAL_REMINDER = booleanPreferencesKey("dream_journal_reminder")
        val DAILY_TOKEN_REMINDER = booleanPreferencesKey("daily_token_reminder")
        val DAILY_TOKEN_REMINDER_TIME = stringPreferencesKey("daily_token_reminder_time")
        val REALITY_CHECK_REMINDER_TIMES = stringPreferencesKey("reality_check_reminder_times")
        val LUCIDITY_FREQUENCY = intPreferencesKey("lucidity_frequency")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val START_TIME = floatPreferencesKey("start_time")
        val END_TIME = floatPreferencesKey("end_time")
    }
    // endregion keys

    private fun parseLocalTimeOrDefault(
        str: String?,
        default: LocalTime = DefaultDreamJournalReminderTime
    ): LocalTime {
        return if (str.isNullOrBlank()) {
            default
        } else {
            try {
                LocalTime.parse(str) // e.g. "07:00"
            } catch (e: Exception) {
                default
            }
        }
    }

    private fun parseRealityCheckTimes(str: String?): List<LocalTime> {
        val parsed = str
            ?.split(",")
            ?.mapNotNull { raw ->
                try {
                    LocalTime.parse(raw.trim())
                } catch (e: Exception) {
                    null
                }
            }
            ?.take(MaxRealityCheckReminders)
            .orEmpty()

        return if (parsed.isEmpty()) {
            DefaultRealityCheckReminderTimes.take(FreeRealityCheckReminderLimit)
        } else {
            val fill = DefaultRealityCheckReminderTimes.drop(parsed.size)
            (parsed + fill).take(MaxRealityCheckReminders)
        }
    }
}
