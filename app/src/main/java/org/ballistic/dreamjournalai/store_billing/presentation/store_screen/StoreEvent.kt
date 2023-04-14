package org.ballistic.dreamjournalai.store_billing.presentation.store_screen

import android.app.Activity

sealed class StoreEvent {
    data class Buy100DreamTokens(val activity: Activity) : StoreEvent()
    data class Buy500DreamTokens(val activity: Activity) : StoreEvent()
}