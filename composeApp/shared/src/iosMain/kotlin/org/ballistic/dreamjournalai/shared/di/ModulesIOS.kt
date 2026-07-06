package org.ballistic.dreamjournalai.shared.di

import com.mikhailovskii.inappreview.InAppReviewDelegate
import com.mikhailovskii.inappreview.appStore.AppStoreInAppReviewInitParams
import com.mikhailovskii.inappreview.appStore.AppStoreInAppReviewManager
import org.ballistic.dreamjournalai.shared.core.domain.DictionaryFileReader
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtil
import org.ballistic.dreamjournalai.shared.core.domain.VibratorUtilImpl
import org.koin.dsl.module
import org.ballistic.dreamjournalai.shared.core.domain.ReviewComponent
import org.ballistic.dreamjournalai.shared.core.util.StoreLinkOpener
import org.ballistic.dreamjournalai.shared.core.util.StoreLinkOpenerIos
import org.ballistic.dreamjournalai.shared.dream_debug.data.IosDebugNotificationTester
import org.ballistic.dreamjournalai.shared.dream_debug.domain.DebugNotificationTester
import org.ballistic.dreamjournalai.shared.dream_notifications.data.IosDailyTokenReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.data.IosDreamReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.data.IosGeneratedArtNotificationSender
import org.ballistic.dreamjournalai.shared.dream_notifications.data.IosPremiumTrialReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DailyTokenReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.DreamReminderScheduler
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.GeneratedArtNotificationSender
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.PremiumTrialReminderScheduler

actual val platformModule = module {
    // iOS doesn't need an Android Context, so we just provide
    // the iOS variant of DictionaryFileReader (which uses NSBundle).
    single { DictionaryFileReader() }

    // If you have iOS-specific things (e.g., Darwin HttpClientEngine,
    // iOS database factories, etc.), define them here as well.
    single<VibratorUtil> {
        VibratorUtilImpl()
    }
    single<InAppReviewDelegate> {
        AppStoreInAppReviewManager(
            AppStoreInAppReviewInitParams(
                appStoreId = "1234567890",
            )
        )
    }

    single {
        ReviewComponent(inAppReviewDelegate = get())
    }

    single<StoreLinkOpener> {
        StoreLinkOpenerIos()
    }

    single<DailyTokenReminderScheduler> {
        IosDailyTokenReminderScheduler()
    }

    single<DreamReminderScheduler> {
        IosDreamReminderScheduler()
    }

    single<DebugNotificationTester> {
        IosDebugNotificationTester()
    }

    single<GeneratedArtNotificationSender> {
        IosGeneratedArtNotificationSender()
    }

    single<PremiumTrialReminderScheduler> {
        IosPremiumTrialReminderScheduler()
    }
}
