package dev.lucasnlm.antimine.di

import android.app.Activity
import android.content.Context
import android.content.Intent
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.external.Achievement
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IBillingManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.Leaderboard
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule = module {
    single {
        object : IInstantAppManager {
            override fun isEnabled(context: Context): Boolean = false
        }
    } bind IInstantAppManager::class

    single {
        object : IBillingManager {
            override fun start() {}

            override fun isEnabled(): Boolean = false

            override suspend fun charge(activity: Activity) {}

            override suspend fun getPrice(): Price? = null

            override suspend fun getPriceFlow(): Flow<Price> = flowOf()

            override fun listenPurchases(): Flow<PurchaseInfo> = flowOf()
        }
    } bind IBillingManager::class

    single {
        object : IPlayGamesManager {
            override suspend fun playerId(): String? = null

            override fun hasGooglePlayGames(): Boolean = false

            override suspend fun silentLogin(): Boolean = false

            override fun showPlayPopUp(activity: Activity) {}

            override fun getLoginIntent(): Intent? = null

            override fun handleLoginResult(data: Intent?) {}

            override fun isLogged(): Boolean = false

            override fun openAchievements(activity: Activity) {}

            override fun openLeaderboards(activity: Activity) {}

            override fun unlockAchievement(achievement: Achievement) {}

            override fun incrementAchievement(achievement: Achievement) {}

            override fun submitLeaderboard(leaderboard: Leaderboard, value: Long) {}

            override fun keepRequestingLogin(status: Boolean) {}

            override fun shouldRequestLogin(): Boolean = false
        }
    } bind IPlayGamesManager::class

    single {
        DebugAnalyticsManager()
    } bind IAnalyticsManager::class
}
