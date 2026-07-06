package org.ballistic.dreamjournalai.shared.dream_main.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.dream_journal_empty_background
import dreamjournalai.composeapp.shared.generated.resources.dream_journal_filled_background
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.BottomNavigationController
import org.ballistic.dreamjournalai.shared.BottomNavigationEvent
import org.ballistic.dreamjournalai.shared.SnackbarAction
import org.ballistic.dreamjournalai.shared.SnackbarController
import org.ballistic.dreamjournalai.shared.SnackbarEvent
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.core.util.StoreLinkOpener
import org.ballistic.dreamjournalai.shared.core.util.StringValue
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.util.OrderType
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DailyTokenReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DreamReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.GeneratedArtPushTokenRegistrar
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationSettingsRepository
import org.jetbrains.compose.resources.DrawableResource

class MainScreenViewModel(
    private val repo: AuthRepository,
    private val vibratorUtil: VibratorUtil,
    private val storeLinkOpener: StoreLinkOpener,
    val dreamUseCases: DreamUseCases,
    private val preferences: DataStore<Preferences>,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val dailyTokenReminderScheduler: DailyTokenReminderScheduler,
    private val dreamReminderScheduler: DreamReminderScheduler,
    private val generatedArtPushTokenRegistrar: GeneratedArtPushTokenRegistrar,
) : ViewModel() {
    private val _mainScreenViewModelState = MutableStateFlow(
        MainScreenViewModelState(
            isUserAnonymous = repo.isUserAnonymous.value,
            backgroundResource = if (repo.isUserAnonymous.value) {
                Res.drawable.dream_journal_empty_background
            } else {
                Res.drawable.dream_journal_filled_background
            },
            isBackgroundResourceResolved = true
        )
    )
    val mainScreenViewModelState: StateFlow<MainScreenViewModelState> = _mainScreenViewModelState.asStateFlow()

    init {
        hydrateCachedDreamJournalBackground()
        observeDreamJournalBackground()
        generatedArtPushTokenRegistrar.start()

        viewModelScope.launch {
            BottomNavigationController.events.collectLatest { event ->
                when (event) {
                    is BottomNavigationEvent.SetVisibility -> {
                        _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                            scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                                bottomBarState = event.visible
                            )
                        )
                    }
                    is BottomNavigationEvent.SetEnabled -> {
                        _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                            isBottomBarEnabledState = event.enabled
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            repo.addDreamTokensFlowListener().collectLatest { resource ->
                if (resource is Resource.Success) {
                    _mainScreenViewModelState.update {
                        it.copy(dreamTokens = resource.data?.toIntOrNull() ?: it.dreamTokens)
                    }
                }
            }
        }

        viewModelScope.launch {
            repo.dreamTokens.collectLatest { tokens ->
                _mainScreenViewModelState.update { it.copy(dreamTokens = tokens) }
            }
        }

        viewModelScope.launch {
            repo.isUserAnonymous.collectLatest { isAnonymous ->
                _mainScreenViewModelState.update { it.copy(isUserAnonymous = isAnonymous) }
            }
        }

        viewModelScope.launch {
            repo.dailyTokenStreak.collectLatest { streak ->
                _mainScreenViewModelState.update { it.copy(dailyTokenStreak = streak) }
            }
        }

        viewModelScope.launch {
            repo.dailyTokenCompletedWeeks.collectLatest { completedWeeks ->
                _mainScreenViewModelState.update { it.copy(dailyTokenCompletedWeeks = completedWeeks) }
            }
        }

        viewModelScope.launch {
            repo.hasClaimedDailyToken.collectLatest { hasClaimed ->
                _mainScreenViewModelState.update { it.copy(hasClaimedDailyToken = hasClaimed) }
            }
        }

        viewModelScope.launch {
            repo.dailyTokensClaimedToday.collectLatest { claimedToday ->
                _mainScreenViewModelState.update { it.copy(dailyTokensClaimedToday = claimedToday) }
            }
        }

        viewModelScope.launch {
            repo.lastDailyTokenClaimDay.collectLatest { lastClaimDay ->
                _mainScreenViewModelState.update { it.copy(lastDailyTokenClaimDay = lastClaimDay) }
            }
        }

        viewModelScope.launch {
            combine(
                notificationSettingsRepository.dailyTokenReminderFlow,
                notificationSettingsRepository.dailyTokenReminderTimeFlow,
                repo.hasClaimedDailyToken
            ) { enabled, reminderTime, hasClaimedToday ->
                Triple(enabled, reminderTime, hasClaimedToday)
            }.collectLatest { (enabled, reminderTime, hasClaimedToday) ->
                if (enabled) {
                    dailyTokenReminderScheduler.syncDailyTokenReminder(
                        hasFullyClaimedToday = hasClaimedToday,
                        reminderTime = reminderTime
                    )
                } else {
                    dailyTokenReminderScheduler.setDailyTokenReminderEnabled(
                        enabled = false,
                        reminderTime = reminderTime
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                notificationSettingsRepository.dreamJournalReminderFlow,
                notificationSettingsRepository.reminderTimeFlow
            ) { enabled, reminderTime ->
                enabled to reminderTime
            }.collectLatest { (enabled, reminderTime) ->
                dreamReminderScheduler.setDreamJournalReminderEnabled(
                    enabled = enabled,
                    reminderTime = reminderTime
                )
            }
        }

        viewModelScope.launch {
            combine(
                notificationSettingsRepository.realityCheckReminderFlow,
                notificationSettingsRepository.lucidityFrequencyFlow,
                notificationSettingsRepository.realityCheckReminderTimesFlow
            ) { enabled, count, reminderTimes ->
                Triple(enabled, count, reminderTimes)
            }.collectLatest { (enabled, count, reminderTimes) ->
                dreamReminderScheduler.setRealityCheckReminders(
                    enabled = enabled,
                    reminderTimes = reminderTimes.take(count)
                )
            }
        }
    }

    private fun hydrateCachedDreamJournalBackground() {
        viewModelScope.launch {
            val userKey = dreamJournalBackgroundCacheKey() ?: run {
                _mainScreenViewModelState.update {
                    it.copy(
                        backgroundResource = Res.drawable.dream_journal_empty_background,
                        isBackgroundResourceResolved = true
                    )
                }
                return@launch
            }
            val cachedHasDreams = preferences.data.first()[userKey]
            if (cachedHasDreams == null && Firebase.auth.currentUser?.isAnonymous != true) {
                return@launch
            }
            _mainScreenViewModelState.update { current ->
                if (current.isBackgroundResourceResolved) {
                    current
                } else {
                    current.copy(
                        backgroundResource = dreamJournalBackgroundResource(cachedHasDreams ?: false),
                        isBackgroundResourceResolved = true
                    )
                }
            }
        }
    }

    private fun observeDreamJournalBackground() {
        viewModelScope.launch {
            dreamUseCases.getDreams(OrderType.Date).collectLatest { dreams ->
                val hasDreams = dreams.isNotEmpty()
                val newResource = dreamJournalBackgroundResource(hasDreams)

                val isSignedInRealAccount = Firebase.auth.currentUser?.let { user ->
                    !user.isAnonymous
                } == true
                if (!hasDreams &&
                    isSignedInRealAccount &&
                    _mainScreenViewModelState.value.backgroundResource == Res.drawable.dream_journal_filled_background
                ) {
                    delay(900)
                }

                _mainScreenViewModelState.update {
                    it.copy(
                        backgroundResource = newResource,
                        isBackgroundResourceResolved = true
                    )
                }
                dreamJournalBackgroundCacheKey()?.let { userKey ->
                    preferences.edit { settings ->
                        settings[userKey] = hasDreams
                    }
                }
            }
        }
    }

    fun onEvent (event: MainScreenEvent) = viewModelScope.launch {
         when (event) {
             is MainScreenEvent.GetAllDreamsForExport -> {
                 dreamUseCases.getDreams(event.orderType).collectLatest { dreams ->
                     event.onDreamsExported(dreams)
                 }
             }
            is MainScreenEvent.SetBottomBarVisibilityState -> {
                viewModelScope.launch {
                    _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                        scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                            bottomBarState = event.state
                        )
                    )
                }
            }
            is MainScreenEvent.SetSearchingState -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                        isUserSearching = event.state
                    )
                )
            }
            is MainScreenEvent.SetTopBarState -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                        topBarState = event.state
                    )
                )
            }
            is MainScreenEvent.SetFloatingActionButtonState -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    scaffoldState = _mainScreenViewModelState.value.scaffoldState.copy(
                        floatingActionButtonState = event.state
                    )
                )
            }
            is MainScreenEvent.SetBackgroundIntroComplete -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    isBackgroundIntroComplete = event.state
                )
            }
            is MainScreenEvent.SetBackgroundBlurComplete -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    isBackgroundBlurComplete = event.state
                )
            }
            is MainScreenEvent.SetAuthTransitionInProgress -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    isAuthTransitionInProgress = event.state
                )
            }
            is MainScreenEvent.SetMainContentHandoffInProgress -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    isMainContentHandoffInProgress = event.state
                )
            }
            is MainScreenEvent.SetDrawerState -> {
                viewModelScope.launch {
                    _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                        isDrawerEnabled = event.state
                    )
                }
            }
            is MainScreenEvent.SearchDreams -> {
                // store plain string in state; composables should handle text input and emit events
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    searchedText = event.query
                )
            }
            is MainScreenEvent.ConsumeDreamTokens -> {
                val result = repo.consumeDreamTokens(event.tokensToConsume)
                if (result is Resource.Error) {
                    result.message?.let { msg ->
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = StringValue.DynamicString(msg),
                                action = SnackbarAction(StringValue.DynamicString("Dismiss")) {
                                    // No need to clear state here, SnackbarController handles it
                                }
                            )
                        )
                    }
                }
            }
            is MainScreenEvent.ClaimDailyDreamTokens -> {
                _mainScreenViewModelState.update { it.copy(isDailyTokenClaimInProgress = true) }
                when (val result = repo.claimDailyDreamTokens()) {
                    is Resource.Success -> {
                        val claim = result.data
                        _mainScreenViewModelState.update {
                            it.copy(
                                isDailyTokenClaimInProgress = false,
                                hasClaimedDailyToken = (claim?.tokensAwardedToday ?: 0) >= (claim?.dailyTokenAllowance ?: 1),
                                dailyTokensClaimedToday = claim?.tokensAwardedToday ?: it.dailyTokensClaimedToday,
                                dailyTokenStreak = claim?.streak ?: it.dailyTokenStreak,
                                dailyTokenCompletedWeeks = claim?.completedWeeks ?: it.dailyTokenCompletedWeeks,
                                lastDailyTokenClaimDay = claim?.claimDay?.takeIf { day -> day.isNotBlank() }
                                    ?: it.lastDailyTokenClaimDay,
                                dreamTokens = claim?.totalTokens ?: it.dreamTokens
                            )
                        }
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = StringValue.DynamicString(
                                    claim?.let {
                                        val plural = if (it.tokensAwarded == 1) "" else "s"
                                        if (it.bonusTokensAwarded > 0) {
                                            "Claimed ${it.tokensAwarded} dream token$plural, including a ${it.bonusTokensAwarded}-token streak bonus!"
                                        } else {
                                            "Claimed ${it.tokensAwarded} dream token$plural."
                                        }
                                    } ?: "Claimed 1 dream token."
                                ),
                                action = SnackbarAction(StringValue.DynamicString("Dismiss")) {}
                            )
                        )
                    }
                    is Resource.Error -> {
                        _mainScreenViewModelState.update {
                            it.copy(
                                isDailyTokenClaimInProgress = false,
                                hasClaimedDailyToken = result.message?.contains("already claimed", ignoreCase = true) == true || it.hasClaimedDailyToken
                            )
                        }
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = StringValue.DynamicString(result.message ?: "Could not claim daily token."),
                                action = SnackbarAction(StringValue.DynamicString("Dismiss")) {}
                            )
                        )
                    }
                    is Resource.Loading -> {
                        _mainScreenViewModelState.update { it.copy(isDailyTokenClaimInProgress = true) }
                    }
                }
            }
            is MainScreenEvent.ToggleDrawerState -> {
                // ViewModel does not own a DrawerState (Compose UI object). Instead, store intent as a boolean.
                 _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                     isDrawerOpen = (event.drawerValue != DrawerValue.Closed)
                 )
             }
            is MainScreenEvent.UserInteracted -> {
                repo.recordUserInteraction()
            }
            is MainScreenEvent.GetAuthState -> {
                repo.getAuthState(viewModelScope)
            }

            is MainScreenEvent.SetBottomBarEnabledState -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    isBottomBarEnabledState = event.state
                )
            }
            is MainScreenEvent.UpdatePaddingValues -> {
                _mainScreenViewModelState.value = _mainScreenViewModelState.value.copy(
                    paddingValues = event.paddingValues
                )
            }

            is MainScreenEvent.SetDreamRecentlySaved -> {
                _mainScreenViewModelState.update{
                    it.copy(
                        isDreamRecentlySaved = event.state
                    )
                }
            }
            is MainScreenEvent.TriggerVibration -> {
                viewModelScope.launch { vibratorUtil.triggerVibration() }
            }
            is MainScreenEvent.OpenStoreLink -> {
                storeLinkOpener.openStoreLink()
            }
        }
    }
}

private fun dreamJournalBackgroundResource(hasDreams: Boolean): DrawableResource {
    return if (hasDreams) {
        Res.drawable.dream_journal_filled_background
    } else {
        Res.drawable.dream_journal_empty_background
    }
}

private fun dreamJournalBackgroundCacheKey(): Preferences.Key<Boolean>? {
    val uid = Firebase.auth.currentUser?.uid?.takeIf { it.isNotBlank() } ?: return null
    return booleanPreferencesKey("dream_journal_has_dreams_$uid")
}

data class MainScreenViewModelState(
    val scaffoldState: ScaffoldState = ScaffoldState(),
    val isDrawerEnabled : Boolean = true,
    val isBottomBarEnabledState : Boolean = true,
    // ViewModel no longer holds a Compose DrawerState; instead expose a simple boolean intent
    val isDrawerOpen: Boolean = false,
    // Keep search text as a plain String for stability; composables handle text input and emit events
    val searchedText: String = "",
    val dreamTokens: Int = 0,
    val isUserAnonymous: Boolean = false,
    val hasClaimedDailyToken: Boolean = false,
    val isDailyTokenClaimInProgress: Boolean = false,
    val dailyTokenStreak: Int = 0,
    val dailyTokenCompletedWeeks: Int = 0,
    val dailyTokensClaimedToday: Int = 0,
    val lastDailyTokenClaimDay: String? = null,
    val backgroundResource: DrawableResource = Res.drawable.dream_journal_empty_background,
    val isBackgroundResourceResolved: Boolean = true,
    val isBackgroundIntroComplete: Boolean = false,
    val isBackgroundBlurComplete: Boolean = false,
    val isAuthTransitionInProgress: Boolean = false,
    val isMainContentHandoffInProgress: Boolean = false,
    val paddingValues: PaddingValues = PaddingValues(0.dp),
    val isDreamRecentlySaved: Boolean = false
)
data class ScaffoldState (
    val bottomBarState: Boolean = true,
    val topBarState: Boolean = true,
    val floatingActionButtonState: Boolean = true,
    val isUserSearching : Boolean = false,
)
