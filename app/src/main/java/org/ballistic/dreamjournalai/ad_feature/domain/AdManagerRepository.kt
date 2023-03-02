package org.ballistic.dreamjournalai.ad_feature.domain

import android.app.Activity

interface AdManagerRepository {
    fun loadRewardedAd(activity: Activity, callback: () -> Unit)
    fun showRewardedAd(activity: Activity, callback: AdCallback)
}
