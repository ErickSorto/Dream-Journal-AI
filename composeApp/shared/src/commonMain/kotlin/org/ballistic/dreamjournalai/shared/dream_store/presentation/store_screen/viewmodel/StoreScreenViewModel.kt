package org.ballistic.dreamjournalai.shared.dream_store.presentation.store_screen.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.functions.functions
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.analytics.AnalyticsUserProperty
import org.ballistic.dreamjournalai.shared.core.analytics.AppAnalytics
import org.ballistic.dreamjournalai.shared.core.analytics.countBucket
import org.ballistic.dreamjournalai.shared.core.platform.getPlatformName
import org.ballistic.dreamjournalai.shared.dream_authentication.domain.repository.AuthRepository
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.model.Dream
import org.ballistic.dreamjournalai.shared.dream_journal_list.domain.use_case.DreamUseCases
import org.ballistic.dreamjournalai.shared.dream_onboarding.presentation.OnboardingAnswers
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.PremiumTrialReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumAnalytics
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumAnalyticsEvent
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumEntrySource
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPackageModel
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPageKind
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPlacement
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPlanOption
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPurchaseResult
import org.ballistic.dreamjournalai.shared.dream_premium.domain.defaultPlanOption
import org.ballistic.dreamjournalai.shared.dream_premium.domain.repository.PremiumPaywallRepository
import org.ballistic.dreamjournalai.shared.dream_premium.domain.trialEndingReminderTriggerAtEpochMillis
import org.ballistic.dreamjournalai.shared.dream_premium.domain.toPremiumPaywallModel
import org.ballistic.dreamjournalai.shared.dream_store.domain.StoreAnalytics
import org.ballistic.dreamjournalai.shared.dream_store.domain.StoreAnalyticsEvent
import org.ballistic.dreamjournalai.shared.dream_store.domain.StoreEvent
import org.ballistic.dreamjournalai.shared.dream_store.domain.repository.BillingRepository

private val logger = Logger.withTag("StoreScreenViewModel")
private const val PremiumPlansUnavailableMessage = "Premium plans are unavailable. Check RevenueCat products and the local StoreKit subscription configuration."

private object DreamTokenProductIds {
    val oneHundred: String
        get() = if (getPlatformName() == "iOS") "dream_tokens_100" else "dream_token_100"

    const val fiveHundred: String = "dream_tokens_500"

    fun tokensFor(productId: String): Int = when (productId) {
        "dream_token_100",
        "dream_tokens_100" -> 100
        fiveHundred -> 500
        else -> 0
    }
}

class StoreScreenViewModel(
    private val billingRepository: BillingRepository,
    private val authRepository: AuthRepository,
    private val premiumPaywallRepository: PremiumPaywallRepository,
    private val premiumTrialReminderScheduler: PremiumTrialReminderScheduler,
    private val appAnalytics: AppAnalytics,
    private val storeAnalytics: StoreAnalytics,
    private val premiumAnalytics: PremiumAnalytics,
    private val dreamUseCases: DreamUseCases,
) : ViewModel() {



    private val _storeScreenViewModelState = MutableStateFlow(StoreScreenViewModelState())
    val storeScreenViewModelState: StateFlow<StoreScreenViewModelState> = _storeScreenViewModelState

    private val productIds: List<String>
        get() = listOf(DreamTokenProductIds.oneHundred, DreamTokenProductIds.fiveHundred)
    private var authStateJob: Job? = null
    private var tokensJob: Job? = null
    private var dreamCountJob: Job? = null
    private var observedTokenUserId: String? = null

    init {
        logger.d { "Initializing StoreScreenViewModel" }

        onEvent(StoreEvent.RefreshPremiumOffer)

        val user = Firebase.auth.currentUser
        _storeScreenViewModelState.update {
            it.copy(isUserAnonymous = user?.isAnonymous == true)
        }

        beginAuthStateListener()
        beginDreamCountListener()
    }

    private fun beginAuthStateListener() {
        if (authStateJob != null) return
        authStateJob = viewModelScope.launch {
            logger.d { "Starting auth state listener" }
            Firebase.auth.authStateChanged.collect { user ->
                logger.d { "Auth state changed: isAnonymous=${user?.isAnonymous} uid=${user?.uid}" }
                appAnalytics.setUserId(user?.uid)
                appAnalytics.setUserProperty(
                    AnalyticsUserProperty.AccountType,
                    when {
                        user == null -> "signed_out"
                        user.isAnonymous -> "anonymous"
                        else -> "registered"
                    }
                )
                _storeScreenViewModelState.update { currentState ->
                    currentState.copy(isUserAnonymous = user?.isAnonymous == true)
                }
                viewModelScope.launch {
                    runCatching {
                        premiumPaywallRepository.syncAppUser(user?.uid)
                        loadPremiumOffer()
                    }.onFailure { error ->
                        logger.e(error) { "Failed to refresh premium offer after auth state change." }
                        _storeScreenViewModelState.update { state ->
                            state.copy(
                                isPremiumOfferLoading = false,
                                premiumPackages = emptyList(),
                                premiumLoadError = PremiumPlansUnavailableMessage
                            )
                        }
                    }
                }
                // Re-bind dream tokens listener only when the user document changes.
                restartTokensListenerIfNeeded(user?.uid)
            }
        }
    }

    private fun stopAuthStateListener() {
        logger.d { "Stopping auth state listener" }
        authStateJob?.cancel()
        authStateJob = null
    }

    private fun beginDreamCountListener() {
        if (dreamCountJob != null) return
        dreamCountJob = viewModelScope.launch {
            dreamUseCases.getDreams().collect { dreams ->
                _storeScreenViewModelState.update { state ->
                    state.copy(dreamCount = dreams.size)
                }
            }
        }
    }

    private fun restartTokensListenerIfNeeded(userId: String? = Firebase.auth.currentUser?.uid) {
        if (tokensJob?.isActive == true && observedTokenUserId == userId) return
        observedTokenUserId = userId
        viewModelScope.launch {
            tokensJob?.cancelAndJoin()
            if (userId == null) {
                _storeScreenViewModelState.update { state ->
                    if (state.dreamTokens == 0) state else state.copy(dreamTokens = 0)
                }
                return@launch
            }
            tokensJob = launch {
                collectDreamTokens()
            }
        }
    }

    private suspend fun collectDreamTokens() {
        logger.d { "(re)collecting dream tokens for current user" }
        authRepository.addDreamTokensFlowListener().collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    val tokens = resource.data?.toIntOrNull() ?: 0
                    _storeScreenViewModelState.update { state ->
                        if (state.dreamTokens == tokens) {
                            state
                        } else {
                            logger.d { "Dream tokens updated: $tokens" }
                            state.copy(dreamTokens = tokens)
                        }
                    }
                }
                is Resource.Error -> {
                    logger.e { "Error fetching dream tokens: ${resource.message}" }
                    // Optionally reset to 0 if logged out
                    _storeScreenViewModelState.update { state ->
                        val isLoggedIn = Firebase.auth.currentUser != null
                        if (!isLoggedIn) state.copy(dreamTokens = 0) else state
                    }
                }
                is Resource.Loading -> {
                    logger.d { "Loading dream tokens..." }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAuthStateListener()
        tokensJob?.cancel()
        tokensJob = null
        dreamCountJob?.cancel()
        dreamCountJob = null
    }

    fun onEvent(event: StoreEvent) {
        logger.d { "Event received: $event" }
        when (event) {
            is StoreEvent.Buy100DreamTokens -> {
                storeAnalytics.track(
                    StoreAnalyticsEvent.TokenProductTapped(
                        productId = DreamTokenProductIds.oneHundred,
                        tokenCount = DreamTokenProductIds.tokensFor(DreamTokenProductIds.oneHundred)
                    )
                )
                viewModelScope.launch {
                    purchaseDreamTokens(DreamTokenProductIds.oneHundred)
                }
            }
            is StoreEvent.Buy500DreamTokens -> {
                storeAnalytics.track(
                    StoreAnalyticsEvent.TokenProductTapped(
                        productId = DreamTokenProductIds.fiveHundred,
                        tokenCount = DreamTokenProductIds.tokensFor(DreamTokenProductIds.fiveHundred)
                    )
                )
                viewModelScope.launch {
                    purchaseDreamTokens(DreamTokenProductIds.fiveHundred)
                }
            }
            is StoreEvent.ToggleLoading -> {
                _storeScreenViewModelState.update {
                    it.copy(isBillingClientLoading = event.isLoading)
                }
            }
            is StoreEvent.GetDreamTokens -> {
                restartTokensListenerIfNeeded()
            }
            is StoreEvent.RefreshPremiumOffer -> {
                viewModelScope.launch {
                    runCatching {
                        premiumPaywallRepository.syncAppUser(Firebase.auth.currentUser?.uid)
                        loadPremiumOffer()
                    }.onFailure { error ->
                        logger.e(error) { "Failed to refresh premium offer." }
                        _storeScreenViewModelState.update { state ->
                            state.copy(
                                isPremiumOfferLoading = false,
                                premiumPackages = emptyList(),
                                premiumLoadError = PremiumPlansUnavailableMessage
                            )
                        }
                    }
                }
            }
            is StoreEvent.SelectPremiumPlan -> {
                val packageId = _storeScreenViewModelState.value.premiumPackageFor(event.plan)
                    ?.packageToPurchase
                    ?.identifier
                storeAnalytics.track(
                    StoreAnalyticsEvent.PremiumPlanSelected(
                        plan = event.plan,
                        packageId = packageId
                    )
                )
                _storeScreenViewModelState.update {
                    it.copy(selectedPremiumPlan = event.plan, premiumPurchaseMessage = null)
                }
            }
            is StoreEvent.PurchaseSelectedPremiumPlan -> {
                viewModelScope.launch {
                    purchaseSelectedPremiumPlan()
                }
            }
        }
    }

    private suspend fun loadPremiumOffer() {
        logger.d { "Loading premium offer for store." }
        _storeScreenViewModelState.update {
            it.copy(isPremiumOfferLoading = true, premiumLoadError = null)
        }

        val offering = runCatching {
            premiumPaywallRepository.getPlacementOffering(PremiumPlacement.SettingsUpgrade.placementId)
        }.onFailure { error ->
            logger.e(error) { "Failed to fetch premium offering." }
        }.getOrNull()

        if (offering == null) {
            storeAnalytics.track(
                StoreAnalyticsEvent.PremiumOfferUnavailable(
                    reason = "offering_null"
                )
            )
            premiumAnalytics.track(PremiumAnalyticsEvent.OfferingMissing(PremiumPlacement.SettingsUpgrade))
            _storeScreenViewModelState.update {
                it.copy(
                    isPremiumOfferLoading = false,
                    premiumPackages = emptyList(),
                    premiumLoadError = PremiumPlansUnavailableMessage
                )
            }
            return
        }

        val model = offering.toPremiumPaywallModel(
            placement = PremiumPlacement.SettingsUpgrade,
            answers = OnboardingAnswers(),
            entrySource = PremiumEntrySource.StoreScreen
        )
        if (model.packages.isEmpty()) {
            logger.e { "RevenueCat offering '${offering.identifier}' has no available packages." }
            storeAnalytics.track(
                StoreAnalyticsEvent.PremiumOfferUnavailable(
                    reason = "empty_packages"
                )
            )
            premiumAnalytics.track(PremiumAnalyticsEvent.OfferingMissing(PremiumPlacement.SettingsUpgrade))
            _storeScreenViewModelState.update {
                it.copy(
                    isPremiumOfferLoading = false,
                    premiumPackages = emptyList(),
                    premiumLoadError = PremiumPlansUnavailableMessage
                )
            }
            return
        }

        val hasPremium = runCatching {
            premiumPaywallRepository.hasPremiumEntitlement(
                premiumPaywallRepository.getCurrentCustomerInfo()
            )
        }.getOrDefault(false)
        appAnalytics.setUserProperty(
            AnalyticsUserProperty.PremiumStatus,
            if (hasPremium) "active" else "free"
        )
        premiumAnalytics.track(
            PremiumAnalyticsEvent.PlacementShown(
                placement = PremiumPlacement.SettingsUpgrade,
                offeringId = offering.identifier,
                entrySource = PremiumEntrySource.StoreScreen
            )
        )
        storeAnalytics.track(
            StoreAnalyticsEvent.PremiumOfferLoaded(
                offeringId = offering.identifier,
                packageCount = model.packages.size,
                hasPremium = hasPremium,
                defaultPlan = model.defaultPlanOption()
            )
        )

        _storeScreenViewModelState.update {
            it.copy(
                isPremiumOfferLoading = false,
                premiumPackages = model.packages,
                selectedPremiumPlan = model.defaultPlanOption(),
                hasPremium = hasPremium,
                premiumLoadError = null
            )
        }
    }

    private suspend fun purchaseSelectedPremiumPlan() {
        val state = _storeScreenViewModelState.value
        val packageToPurchase = state.packageForSelectedPremiumPlan()
        if (packageToPurchase == null) {
            storeAnalytics.track(
                StoreAnalyticsEvent.PremiumPurchaseCompleted(
                    result = "missing_package",
                    plan = state.selectedPremiumPlan,
                    packageId = null,
                    message = "Premium plans are still loading."
                )
            )
            _storeScreenViewModelState.update {
                it.copy(premiumPurchaseMessage = "Premium plans are still loading.")
            }
            return
        }

        storeAnalytics.track(
            StoreAnalyticsEvent.PremiumPurchaseStarted(
                plan = state.selectedPremiumPlan,
                packageId = packageToPurchase.packageToPurchase.identifier
            )
        )
        premiumAnalytics.track(
            PremiumAnalyticsEvent.PurchaseStarted(
                page = PremiumPageKind.OfferSheet,
                packageId = packageToPurchase.packageToPurchase.identifier
            )
        )
        _storeScreenViewModelState.update {
            it.copy(isPremiumPurchaseInProgress = true, premiumPurchaseMessage = null)
        }

        val result = premiumPaywallRepository.purchasePackage(packageToPurchase.packageToPurchase)
        when (result) {
            is PremiumPurchaseResult.Success -> {
                val conversionSnapshot = buildStoreConversionSnapshot(
                    conversionSource = "store_screen",
                    packageModel = packageToPurchase
                )
                storeAnalytics.track(
                    StoreAnalyticsEvent.PremiumPurchaseCompleted(
                        result = "success",
                        plan = state.selectedPremiumPlan,
                        packageId = result.packageId,
                        properties = conversionSnapshot
                    )
                )
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchaseSucceeded(
                        page = PremiumPageKind.OfferSheet,
                        packageId = result.packageId,
                        properties = conversionSnapshot
                    )
                )
                appAnalytics.setUserProperty(AnalyticsUserProperty.PremiumStatus, "active")
                syncPremiumTrialReminder(packageToPurchase)
                _storeScreenViewModelState.update {
                    it.copy(
                        isPremiumPurchaseInProgress = false,
                        hasPremium = true,
                        premiumPurchaseMessage = "Premium is active."
                    )
                }
            }
            PremiumPurchaseResult.AlreadySubscribed -> {
                storeAnalytics.track(
                    StoreAnalyticsEvent.PremiumPurchaseCompleted(
                        result = "already_active",
                        plan = state.selectedPremiumPlan,
                        packageId = packageToPurchase.packageToPurchase.identifier
                    )
                )
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchaseAlreadyActive(
                        page = PremiumPageKind.OfferSheet,
                        packageId = packageToPurchase.packageToPurchase.identifier
                    )
                )
                appAnalytics.setUserProperty(AnalyticsUserProperty.PremiumStatus, "active")
                premiumTrialReminderScheduler.cancelTrialEndingReminder()
                _storeScreenViewModelState.update {
                    it.copy(
                        isPremiumPurchaseInProgress = false,
                        hasPremium = true,
                        premiumPurchaseMessage = "Premium is active."
                    )
                }
            }
            PremiumPurchaseResult.UserCancelled -> {
                storeAnalytics.track(
                    StoreAnalyticsEvent.PremiumPurchaseCompleted(
                        result = "cancelled",
                        plan = state.selectedPremiumPlan,
                        packageId = packageToPurchase.packageToPurchase.identifier
                    )
                )
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchaseCancelled(
                        page = PremiumPageKind.OfferSheet,
                        packageId = packageToPurchase.packageToPurchase.identifier
                    )
                )
                _storeScreenViewModelState.update {
                    it.copy(isPremiumPurchaseInProgress = false)
                }
            }
            PremiumPurchaseResult.Pending -> {
                storeAnalytics.track(
                    StoreAnalyticsEvent.PremiumPurchaseCompleted(
                        result = "pending",
                        plan = state.selectedPremiumPlan,
                        packageId = packageToPurchase.packageToPurchase.identifier
                    )
                )
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchasePending(
                        page = PremiumPageKind.OfferSheet,
                        packageId = packageToPurchase.packageToPurchase.identifier
                    )
                )
                _storeScreenViewModelState.update {
                    it.copy(
                        isPremiumPurchaseInProgress = false,
                        premiumPurchaseMessage = "Purchase pending. We'll unlock Premium when it completes."
                    )
                }
            }
            is PremiumPurchaseResult.Error -> {
                logger.e { "Premium purchase failed: ${result.message}" }
                storeAnalytics.track(
                    StoreAnalyticsEvent.PremiumPurchaseCompleted(
                        result = "error",
                        plan = state.selectedPremiumPlan,
                        packageId = packageToPurchase.packageToPurchase.identifier,
                        message = result.message
                    )
                )
                premiumAnalytics.track(
                    PremiumAnalyticsEvent.PurchaseFailed(
                        page = PremiumPageKind.OfferSheet,
                        packageId = packageToPurchase.packageToPurchase.identifier,
                        message = result.message
                    )
                )
                _storeScreenViewModelState.update {
                    it.copy(
                        isPremiumPurchaseInProgress = false,
                        premiumPurchaseMessage = result.message
                    )
                }
            }
        }
    }

    private suspend fun syncPremiumTrialReminder(packageModel: PremiumPackageModel) {
        val triggerAtEpochMillis = packageModel.trialEndingReminderTriggerAtEpochMillis()
        if (triggerAtEpochMillis != null) {
            premiumTrialReminderScheduler.scheduleTrialEndingReminder(triggerAtEpochMillis)
        } else {
            premiumTrialReminderScheduler.cancelTrialEndingReminder()
        }
    }

    private suspend fun buildStoreConversionSnapshot(
        conversionSource: String,
        packageModel: PremiumPackageModel,
    ): Map<String, Any?> {
        val dreams = currentDreamsForAnalytics()
        val state = _storeScreenViewModelState.value
        return dreamStateSnapshot(dreams) + mapOf(
            "conversion_source" to conversionSource,
            "surface" to "store",
            "selected_plan" to state.selectedPremiumPlan.name.lowercase(),
            "selected_package_id" to packageModel.packageToPurchase.identifier,
            "has_trial" to packageModel.hasTrial,
            "token_balance" to state.dreamTokens,
            "token_balance_bucket" to countBucket(state.dreamTokens),
            "daily_token_streak" to authRepository.dailyTokenStreak.value,
            "has_generated_world" to authRepository.hasGeneratedDreamWorld.value
        )
    }

    private suspend fun currentDreamsForAnalytics(): List<Dream> {
        return runCatching {
            dreamUseCases.getDreams().first()
        }.getOrDefault(emptyList())
    }

    private fun dreamStateSnapshot(dreams: List<Dream>): Map<String, Any> {
        val dreamCount = dreams.size
        return mapOf(
            "dream_count" to dreamCount,
            "dream_count_bucket" to countBucket(dreamCount),
            "has_saved_dream" to dreams.isNotEmpty(),
            "ai_interpretation_count" to dreams.count { it.AIResponse.isNotBlank() },
            "ai_art_count" to dreams.count { it.generatedImage.isNotBlank() },
            "audio_dream_count" to dreams.count { it.audioDuration > 0 || it.audioUrl.isNotBlank() },
            "lucid_dream_count" to dreams.count { it.isLucid },
            "nightmare_count" to dreams.count { it.isNightmare },
            "recurring_dream_count" to dreams.count { it.isRecurring }
        )
    }

    private suspend fun purchaseDreamTokens(productId: String) {
        logger.d { "Starting purchase flow for productId: $productId" }
        val tokenCount = DreamTokenProductIds.tokensFor(productId)
        storeAnalytics.track(
            StoreAnalyticsEvent.TokenPurchaseStarted(
                productId = productId,
                tokenCount = tokenCount
            )
        )
        _storeScreenViewModelState.update { it.copy(isBillingClientLoading = true) }

        val storeProducts = fetchStoreProducts()
        logger.d { "Store products available: ${storeProducts.map { it.id }}" }
        val storeProduct = storeProducts.find { it.id == productId }

        if (storeProduct != null) {
            logger.d { "Product found: $storeProduct" }
            billingRepository.purchaseProduct(
                product = storeProduct,
                onError = { error, userCancelled ->
                    logger.e { "Purchase failed for $productId: $error, userCancelled=$userCancelled" }
                    storeAnalytics.track(
                        StoreAnalyticsEvent.TokenPurchaseCompleted(
                            result = if (userCancelled) "cancelled" else "error",
                            productId = productId,
                            tokenCount = tokenCount,
                            message = error.toString()
                        )
                    )
                    _storeScreenViewModelState.update { it.copy(isBillingClientLoading = false) }
                },
                onSuccess = { storeTransaction, customerInfo ->
                    logger.d { "Purchase successful: Transaction=$storeTransaction" }
                    storeAnalytics.track(
                        StoreAnalyticsEvent.TokenPurchaseCompleted(
                            result = "store_success",
                            productId = productId,
                            tokenCount = tokenCount
                        )
                    )
                    viewModelScope.launch {
                        handlePurchaseVerification(storeTransaction)
                    }
                }
            )
        } else {
            logger.e { "Product not found for productId: $productId" }
            storeAnalytics.track(
                StoreAnalyticsEvent.TokenPurchaseCompleted(
                    result = "product_not_found",
                    productId = productId,
                    tokenCount = tokenCount
                )
            )
            _storeScreenViewModelState.update { it.copy(isBillingClientLoading = false) }
        }
    }

    private suspend fun handlePurchaseVerification(storeTransaction: StoreTransaction) {
        logger.d { "Verifying single-item purchase." }

        // 1) Get the first (and only) product ID
        val productId = storeTransaction.productIds.firstOrNull()
        if (productId == null) {
            logger.e { "No productId found in storeTransaction!" }
            storeAnalytics.track(
                StoreAnalyticsEvent.TokenVerificationCompleted(
                    result = "missing_product_id",
                    productId = "unknown",
                    tokenCount = 0
                )
            )
            _storeScreenViewModelState.update { it.copy(isBillingClientLoading = false) }
            return
        }

        // 2) Determine how many tokens to award for this product
        val tokensToAward = DreamTokenProductIds.tokensFor(productId)
        if (tokensToAward == 0) {
            logger.e { "Invalid productId: $productId" }
            storeAnalytics.track(
                StoreAnalyticsEvent.TokenVerificationCompleted(
                    result = "invalid_product_id",
                    productId = productId,
                    tokenCount = 0
                )
            )
            _storeScreenViewModelState.update { it.copy(isBillingClientLoading = false) }
            return
        }

        // 3) Verify the purchase on your server
        val verified = verifyPurchaseOnServer(
            productId = productId,
            transactionId = storeTransaction.transactionId
                ?: "$productId-${storeTransaction.purchaseTime}",
            purchaseTime = storeTransaction.purchaseTime
        )

        // 4) Stop loading and let the Firestore token listener reflect the confirmed balance.
        if (verified) {
            logger.d { "Purchase verified for $productId, awarding $tokensToAward tokens" }
            storeAnalytics.track(
                StoreAnalyticsEvent.TokenVerificationCompleted(
                    result = "verified",
                    productId = productId,
                    tokenCount = tokensToAward
                )
            )
        } else {
            logger.e { "Purchase verification failed for $productId" }
            storeAnalytics.track(
                StoreAnalyticsEvent.TokenVerificationCompleted(
                    result = "failed",
                    productId = productId,
                    tokenCount = tokensToAward
                )
            )
        }

        // 5) Stop loading indicator
        _storeScreenViewModelState.update { it.copy(isBillingClientLoading = false) }
    }


    private suspend fun fetchStoreProducts(): List<StoreProduct> =
        suspendCancellableCoroutine { cont ->
            logger.d { "Fetching store products..." }
            billingRepository.fetchProducts(
                productIds = productIds,
                onError = { error ->
                    logger.e { "Error fetching products: $error" }
                    storeAnalytics.track(
                        StoreAnalyticsEvent.TokenProductsFetched(
                            result = "error",
                            productCount = 0,
                            requestedCount = productIds.size,
                            message = error.toString()
                        )
                    )
                    cont.resume(emptyList()) { cause, _, _ -> }
                },
                onSuccess = { storeProducts ->
                    logger.d { "Products fetched successfully: $storeProducts" }
                    storeAnalytics.track(
                        StoreAnalyticsEvent.TokenProductsFetched(
                            result = "success",
                            productCount = storeProducts.size,
                            requestedCount = productIds.size
                        )
                    )
                    cont.resume(storeProducts) { cause, _, _ -> }
                }
            )
        }

    private suspend fun verifyPurchaseOnServer(
        productId: String,
        transactionId: String,
        purchaseTime: Long
    ): Boolean {
        val userId = Firebase.auth.currentUser?.uid ?: return false

        // Determine tokens for this one product
        val tokensToAward = DreamTokenProductIds.tokensFor(productId)
        if (tokensToAward == 0) {
            logger.e { "Invalid productId: $productId" }
            return false
        }

        val data = hashMapOf(
            "productId" to productId,
            "transactionId" to transactionId,
            "purchaseTime" to purchaseTime,
            "userId" to userId,
            "dreamTokens" to tokensToAward
        )

        return try {
            val response = Firebase.functions
                .httpsCallable("verifyDreamTokenPurchase")
                .invoke(data)

            // Deserialize the response
            val verificationResponse = response.data<VerificationResponse>()
            logger.d {
                "Server verification response: success=${verificationResponse.success}, " +
                        "tokensAwarded=${verificationResponse.tokensAwarded}, " +
                        "totalTokens=${verificationResponse.totalTokens}, " +
                        "alreadyProcessed=${verificationResponse.alreadyProcessed}"
            }
            if (verificationResponse.success) {
                _storeScreenViewModelState.update {
                    it.copy(dreamTokens = verificationResponse.totalTokens)
                }
            }
            verificationResponse.success
        } catch (e: Exception) {
            logger.e { "Server verification failed: $e" }
            false
        }
    }


}


data class StoreScreenViewModelState(
    val isBillingClientLoading: Boolean = false,
    val isUserAnonymous: Boolean = false,
    val dreamTokens: Int = 0,
    val dreamCount: Int = 0,
    val isPremiumOfferLoading: Boolean = false,
    val isPremiumPurchaseInProgress: Boolean = false,
    val hasPremium: Boolean = false,
    val selectedPremiumPlan: PremiumPlanOption = PremiumPlanOption.Annual,
    val premiumPackages: List<PremiumPackageModel> = emptyList(),
    val premiumLoadError: String? = null,
    val premiumPurchaseMessage: String? = null
) {
    fun packageForSelectedPremiumPlan(): PremiumPackageModel? {
        return when (selectedPremiumPlan) {
            PremiumPlanOption.Annual -> premiumPackages.firstOrNull { it.isAnnual }
                ?: premiumPackages.firstOrNull()
            PremiumPlanOption.Monthly -> premiumPackages.firstOrNull { it.isMonthly }
                ?: premiumPackages.firstOrNull()
        }
    }

    fun premiumPackageFor(plan: PremiumPlanOption): PremiumPackageModel? {
        return premiumPackages.firstOrNull {
            when (plan) {
                PremiumPlanOption.Annual -> it.isAnnual
                PremiumPlanOption.Monthly -> it.isMonthly
            }
        }
    }
}

@Serializable
data class VerificationResponse(
    val success: Boolean = false,
    val alreadyProcessed: Boolean = false,
    val tokensAwarded: Int = 0,
    val totalTokens: Int = 0,
)
