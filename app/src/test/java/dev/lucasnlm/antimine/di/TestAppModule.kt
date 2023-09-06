package dev.lucasnlm.antimine.di

import android.app.Activity
import android.content.Context
import android.content.Intent
import dev.lucasnlm.antimine.core.analytics.DebugAnalyticsManager
import dev.lucasnlm.external.Achievement
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.InstantAppManager
import dev.lucasnlm.external.Leaderboard
import dev.lucasnlm.external.PlayGamesManager
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.bind
import org.koin.dsl.module

val AppModule =
    module {
        single {
            object : InstantAppManager {
                override fun isEnabled(context: Context): Boolean = false
            }
        } bind InstantAppManager::class

        single {
            object : BillingManager {
                override fun start() {}

                override fun isEnabled(): Boolean = false

                override suspend fun charge(activity: Activity) {}

                override suspend fun getPrice(): Price? = null

                override suspend fun getPriceFlow(): Flow<Price> = flowOf()

                override fun listenPurchases(): Flow<PurchaseInfo> = flowOf()
            }
        } bind BillingManager::class

        single {
            object : PlayGamesManager {
                override suspend fun playerId(): String? = null

                override fun hasGooglePlayGames(): Boolean = false

                override suspend fun silentLogin(): Boolean = false

                override fun showPlayPopUp(activity: Activity) {}

                override fun getLoginIntent(): Intent? = null

                override fun handleLoginResult(data: Intent?) {}

                override fun isLogged(): Boolean = false

                override fun openAchievements(activity: Activity) {}

                override fun openLeaderboards(activity: Activity) {}

                override suspend fun incrementAchievement(
                    achievement: Achievement,
                    value: Int,
                ) {}

                override suspend fun setAchievementSteps(
                    achievement: Achievement,
                    value: Int,
                ) {}

                override suspend fun unlockAchievement(achievement: Achievement) {}

                override fun submitLeaderboard(
                    leaderboard: Leaderboard,
                    value: Long,
                ) {}

                override fun keepRequestingLogin(status: Boolean) {}

                override fun shouldRequestLogin(): Boolean = false

                override fun signInToFirebase(activity: Activity) {}
            }
        } bind PlayGamesManager::class

        single {
            DebugAnalyticsManager()
        } bind AnalyticsManager::class
    }
