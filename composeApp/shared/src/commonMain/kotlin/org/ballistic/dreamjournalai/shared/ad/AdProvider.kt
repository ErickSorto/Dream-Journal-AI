package org.ballistic.dreamjournalai.shared.ad

import androidx.compose.runtime.Composable

@Composable
expect fun RewardedAd(
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: () -> Unit,
    onAdReward: () -> Unit
)
