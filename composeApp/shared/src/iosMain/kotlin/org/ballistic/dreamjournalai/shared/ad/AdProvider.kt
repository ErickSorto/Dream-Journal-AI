package org.ballistic.dreamjournalai.shared.ad

import androidx.compose.runtime.Composable

@Composable
actual fun RewardedAd(
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: () -> Unit,
    onAdReward: () -> Unit
) {
    // TODO: Implement iOS Ads
}
