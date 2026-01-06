package org.ballistic.dreamjournalai.shared.core.components

import androidx.compose.runtime.Composable
import org.ballistic.dreamjournalai.shared.ad.RewardedAd

@Composable
fun RewardedAdComposable(
    onAdDismissed: () -> Unit,
    onRewardEarned: () -> Unit,
    onAdFailed: () -> Unit
) {
    RewardedAd(
        onAdLoaded = {},
        onAdFailedToLoad = onAdFailed,
        onAdReward = onRewardEarned
    )
}
