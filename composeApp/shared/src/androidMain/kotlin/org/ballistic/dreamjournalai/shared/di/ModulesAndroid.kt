package org.ballistic.dreamjournalai.shared.di

import android.app.Activity
import com.mikhailovskii.inappreview.InAppReviewDelegate
import com.mikhailovskii.inappreview.googlePlay.GooglePlayInAppReviewInitParams
import com.mikhailovskii.inappreview.googlePlay.GooglePlayInAppReviewManager
import org.ballistic.dreamjournalai.shared.core.domain.DictionaryFileReader
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtilImpl
import org.ballistic.dreamjournalai.shared.core.util.StoreLinkOpener
import org.ballistic.dreamjournalai.shared.core.util.StoreLinkOpenerAndroid
import org.ballistic.dreamjournalai.shared.dream_debug.data.AndroidDebugNotificationTester
import org.ballistic.dreamjournalai.shared.dream_debug.domain.DebugNotificationTester
import org.ballistic.dreamjournalai.shared.dream_notifications.data.AndroidDailyTokenReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.data.AndroidDreamReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.data.AndroidGeneratedArtNotificationSender
import org.ballistic.dreamjournalai.shared.dream_notifications.data.AndroidPremiumTrialReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DailyTokenReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DreamReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.GeneratedArtNotificationSender
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.PremiumTrialReminderScheduler
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single {
        DictionaryFileReader(androidApplication())
    }

    single<VibratorUtil> {
        VibratorUtilImpl(context = androidApplication())
    }

    factory<InAppReviewDelegate> { (act: Activity) ->
        GooglePlayInAppReviewManager(
            GooglePlayInAppReviewInitParams(act)
        )
    }

    single<StoreLinkOpener> {
        StoreLinkOpenerAndroid(context = androidContext())
    }

    single<DailyTokenReminderScheduler> {
        AndroidDailyTokenReminderScheduler(context = androidContext())
    }

    single<DreamReminderScheduler> {
        AndroidDreamReminderScheduler(context = androidContext())
    }

    single<DebugNotificationTester> {
        AndroidDebugNotificationTester(context = androidContext())
    }

    single<GeneratedArtNotificationSender> {
        AndroidGeneratedArtNotificationSender(context = androidContext())
    }

    single<PremiumTrialReminderScheduler> {
        AndroidPremiumTrialReminderScheduler(context = androidContext())
    }
}
