package org.ballistic.dreamjournalai.shared

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.ballistic.dreamjournalai.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

class DreamJournalAIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        enableCrashlytics()

        createNotificationChannel()
        initializeWorkManager()

        initKoin {
            androidContext(this@DreamJournalAIApp)
        }

        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            apiKey = "goog_gvSGrQtANSlegqEvLGguJATSvTG" //TODO: Add your RevenueCat API Key
        ) {
            // If you have an authenticated user, set this to their ID
            // or leave it null to let RevenueCat create an anonymous ID
            appUserId = Firebase.auth.currentUser?.uid
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun initializeWorkManager() {
        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
        WorkManager.initialize(this, configuration)
    }

    companion object {
        const val CHANNEL_ID = "dream_journal_channel"
        private var instance: DreamJournalAIApp? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
}