package org.ballistic.dreamjournalai.shared.ad

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

@Composable
actual fun RewardedAd(
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: () -> Unit,
    onAdReward: () -> Unit
) {
    val context = LocalContext.current
    val adRequest = remember { AdRequest.Builder().build() }

    RewardedAd.load(
        context,
        "ca-app-pub-3940256099942544/5224354917",
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                onAdLoaded()
                ad.show(context as Activity) {
                    onAdReward()
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                onAdFailedToLoad()
            }
        }
    )
}
