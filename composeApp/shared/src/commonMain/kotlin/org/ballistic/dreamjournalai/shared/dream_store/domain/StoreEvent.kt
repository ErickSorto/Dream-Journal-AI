package org.ballistic.dreamjournalai.shared.dream_store.domain

import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumPlanOption

sealed class StoreEvent {
    data object Buy100DreamTokens : StoreEvent()
    data object Buy500DreamTokens : StoreEvent()
    data class SelectPremiumPlan(val plan: PremiumPlanOption) : StoreEvent()
    data object PurchaseSelectedPremiumPlan : StoreEvent()
    data object RefreshPremiumOffer : StoreEvent()

    data class ToggleLoading(val isLoading: Boolean) : StoreEvent()
    data object GetDreamTokens : StoreEvent()
}
