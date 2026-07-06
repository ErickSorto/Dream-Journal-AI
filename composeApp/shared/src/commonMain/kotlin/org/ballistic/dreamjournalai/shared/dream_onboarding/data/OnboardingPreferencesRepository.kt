package org.ballistic.dreamjournalai.shared.dream_onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.ballistic.dreamjournalai.shared.core.Constants.USERS

interface OnboardingPreferencesRepository {
    val hasCompletedOnboarding: Flow<Boolean>
    val completionMode: Flow<String?>

    suspend fun markStartedForCurrentUser()
    suspend fun markCompleted(completionMode: String)
    suspend fun hasCompletedOnboardingForCurrentUser(): Boolean
}

class DefaultOnboardingPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : OnboardingPreferencesRepository {

    override val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.HasCompletedOnboarding] ?: false
    }

    override val completionMode: Flow<String?> = dataStore.data.map { preferences ->
        preferences[Keys.CompletionMode]
    }

    override suspend fun markCompleted(completionMode: String) {
        val user = auth.currentUser
        dataStore.edit { preferences ->
            preferences[Keys.HasCompletedOnboarding] = true
            preferences[Keys.CompletionMode] = completionMode
            preferences.remove(Keys.HasStartedOnboarding)
            preferences.remove(Keys.StartedUserId)
            user?.uid?.let { uid ->
                preferences[Keys.CompletedUserId] = uid
            }
        }

        user ?: return
        syncRemoteCompletion(user.uid, completionMode)
    }

    override suspend fun markStartedForCurrentUser() {
        val user = auth.currentUser ?: return
        val preferences = dataStore.data.first()
        val localHasCompleted = preferences[Keys.HasCompletedOnboarding] ?: false
        val localCompletedUserId = preferences[Keys.CompletedUserId]
        val localCompletionBelongsToCurrentUser =
            localHasCompleted && (localCompletedUserId == null || localCompletedUserId == user.uid)

        if (localCompletionBelongsToCurrentUser) return

        dataStore.edit { editablePreferences ->
            editablePreferences[Keys.HasStartedOnboarding] = true
            editablePreferences[Keys.StartedUserId] = user.uid
        }

        syncRemoteStart(user.uid)
    }

    override suspend fun hasCompletedOnboardingForCurrentUser(): Boolean {
        val preferences = dataStore.data.first()
        val localHasCompleted = preferences[Keys.HasCompletedOnboarding] ?: false
        val localCompletionMode = preferences[Keys.CompletionMode]
        val localCompletedUserId = preferences[Keys.CompletedUserId]
        val user = auth.currentUser ?: return localHasCompleted
        val localCompletionBelongsToCurrentUser =
            localHasCompleted && (localCompletedUserId == null || localCompletedUserId == user.uid)

        if (localCompletionBelongsToCurrentUser) {
            syncRemoteCompletion(
                uid = user.uid,
                completionMode = localCompletionMode ?: DEFAULT_COMPLETION_MODE
            )
            if (localCompletedUserId == null) {
                dataStore.edit { editablePreferences ->
                    editablePreferences[Keys.CompletedUserId] = user.uid
                }
            }
            return true
        }

        val snapshot = runCatching {
            db.collection(USERS).document(user.uid).get()
        }.getOrNull() ?: return false

        if (!snapshot.exists) return false

        val remoteHasCompletedResult = runCatching {
            snapshot.get<Boolean>(REMOTE_HAS_COMPLETED_ONBOARDING)
        }

        val hasCompleted = remoteHasCompletedResult.getOrElse {
            true
        }

        if (hasCompleted) {
            val remoteCompletionMode = runCatching {
                snapshot.get<String>(REMOTE_COMPLETION_MODE)
            }.getOrNull() ?: LEGACY_COMPLETION_MODE

            dataStore.edit { preferences ->
                preferences[Keys.HasCompletedOnboarding] = true
                preferences[Keys.CompletionMode] = remoteCompletionMode
                preferences[Keys.CompletedUserId] = user.uid
                preferences.remove(Keys.HasStartedOnboarding)
                preferences.remove(Keys.StartedUserId)
            }

            if (remoteHasCompletedResult.isFailure) {
                syncRemoteCompletion(
                    uid = user.uid,
                    completionMode = remoteCompletionMode
                )
            }
        }

        return hasCompleted
    }

    private suspend fun syncRemoteCompletion(uid: String, completionMode: String) {
        runCatching {
            db.collection(USERS)
                .document(uid)
                .set(
                    data = mapOf(
                        REMOTE_HAS_COMPLETED_ONBOARDING to true,
                        REMOTE_HAS_STARTED_ONBOARDING to false,
                        REMOTE_COMPLETION_MODE to completionMode,
                        REMOTE_COMPLETED_AT to FieldValue.serverTimestamp
                    ),
                    merge = true
                )
        }
    }

    private suspend fun syncRemoteStart(uid: String) {
        runCatching {
            db.collection(USERS)
                .document(uid)
                .set(
                    data = mapOf(
                        REMOTE_HAS_STARTED_ONBOARDING to true,
                        REMOTE_HAS_COMPLETED_ONBOARDING to false,
                        REMOTE_STARTED_AT to FieldValue.serverTimestamp
                    ),
                    merge = true
                )
        }
    }

    private object Keys {
        val HasCompletedOnboarding = booleanPreferencesKey("onboarding_completed")
        val HasStartedOnboarding = booleanPreferencesKey("onboarding_started")
        val CompletionMode = stringPreferencesKey("onboarding_completion_mode")
        val StartedUserId = stringPreferencesKey("onboarding_started_user_id")
        val CompletedUserId = stringPreferencesKey("onboarding_completed_user_id")
    }

    private companion object {
        const val DEFAULT_COMPLETION_MODE = "free_plan"
        const val LEGACY_COMPLETION_MODE = "legacy_existing_user"
        const val REMOTE_HAS_STARTED_ONBOARDING = "hasStartedOnboarding"
        const val REMOTE_HAS_COMPLETED_ONBOARDING = "hasCompletedOnboarding"
        const val REMOTE_COMPLETION_MODE = "onboardingCompletionMode"
        const val REMOTE_STARTED_AT = "onboardingStartedAt"
        const val REMOTE_COMPLETED_AT = "onboardingCompletedAt"
    }
}
