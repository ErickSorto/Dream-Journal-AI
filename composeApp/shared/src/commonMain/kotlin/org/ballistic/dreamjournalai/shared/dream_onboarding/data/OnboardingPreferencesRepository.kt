package org.ballistic.dreamjournalai.shared.dream_onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface OnboardingPreferencesRepository {
    val hasCompletedOnboarding: Flow<Boolean>
    val completionMode: Flow<String?>

    suspend fun markCompleted(completionMode: String)
}

class DefaultOnboardingPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : OnboardingPreferencesRepository {

    override val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.HasCompletedOnboarding] ?: false
    }

    override val completionMode: Flow<String?> = dataStore.data.map { preferences ->
        preferences[Keys.CompletionMode]
    }

    override suspend fun markCompleted(completionMode: String) {
        dataStore.edit { preferences ->
            preferences[Keys.HasCompletedOnboarding] = true
            preferences[Keys.CompletionMode] = completionMode
        }
    }

    private object Keys {
        val HasCompletedOnboarding = booleanPreferencesKey("onboarding_completed")
        val CompletionMode = stringPreferencesKey("onboarding_completion_mode")
    }
}
