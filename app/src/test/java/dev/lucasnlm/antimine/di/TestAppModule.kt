package dev.lucasnlm.antimine.di

import android.app.Activity
import android.content.Context
import android.content.Intent
import dev.lucasnlm.antimine.common.BuildConfig
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.IAnalyticsManager
import dev.lucasnlm.antimine.core.analytics.ProdAnalyticsManager
import dev.lucasnlm.antimine.share.ShareManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.ExternalAnalyticsWrapper
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.PlayGamesManager
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule = module {
    single {
        object : IInstantAppManager {
            override fun isEnabled(): Boolean = false

            override fun isInstantAppSupported(context: Context): Boolean = false

            override fun isInAppPaymentsSupported(context: Context): Boolean = false

            override fun showInstallPrompt(
                activity: Activity,
                intent: Intent?,
                requestCode: Int,
                referrer: String?
            ): Boolean = false
        }
    } bind IInstantAppManager::class

    single {
        object : IBillingManager {
            override fun start() { }

            override suspend fun charge(activity: Activity) { }
        }
    } bind IBillingManager::class

    single { object : IPlayGamesManager {
        override fun hasGooglePlayGames(): Boolean = false

        override fun silentLogin(activity: Activity) { }

        override fun getLoginIntent(): Intent? = null

        override fun handleLoginResult(data: Intent?) { }

        override fun isLogged(): Boolean = false

        override fun openAchievements(activity: Activity) { }

        override fun openLeaderboards(activity: Activity) { }

    } } bind IPlayGamesManager::class

    single { ShareManager(get()) }

    single {
        DebugAnalyticsManager()
    } bind IAnalyticsManager::class
}
