package org.ballistic.dreamjournalai.shared.ad

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

interface IosRewardedAdHandler {
    fun showRewardedAd(
        onAdLoaded: () -> Unit,
        onAdFailedToLoad: () -> Unit,
        onAdReward: () -> Unit
    )
}

object IosRewardedAdBridge {
    var handler: IosRewardedAdHandler? = null
}

@Composable
actual fun RewardedAd(
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: () -> Unit,
    onAdReward: () -> Unit
) {
    LaunchedEffect(Unit) {
        IosRewardedAdBridge.handler?.showRewardedAd(
            onAdLoaded = onAdLoaded,
            onAdFailedToLoad = onAdFailedToLoad,
            onAdReward = onAdReward
        ) ?: onAdFailedToLoad()
    }
}
