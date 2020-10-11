package dev.lucasnlm.antimine.splash.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.core.control.ControlStyle
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.support.IapHandler
import dev.lucasnlm.external.ICloudStorageManager
import dev.lucasnlm.external.IInstantAppManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.model.CloudSave

class SplashViewModel(
    private val context: Context,
    private val preferencesRepository: IPreferencesRepository,
    private val statsRepository: IStatsRepository,
    private val saveCloudStorageManager: ICloudStorageManager,
    private val playGamesManager: IPlayGamesManager,
    private val instantAppManager: IInstantAppManager,
    private val iapHandler: IapHandler,
) : ViewModel() {
    fun startIap() {
        iapHandler.start()
    }

    suspend fun migrateCloudSave() {
        if (instantAppManager.isEnabled(context) || preferencesRepository.shouldMigrateFromCloud()) {
            val userId = playGamesManager.playerId()

            userId?.let {
                saveCloudStorageManager.getSave(it)?.let { cloudSave ->
                    loadCloudSave(cloudSave)
                }
            }
        }

        if (instantAppManager.isEnabled(context)) {
            preferencesRepository.setMigrateFromCloud(true)
        }
    }

    private suspend fun loadCloudSave(cloudSave: CloudSave) = with(cloudSave) {
        if (cloudSave.completeTutorial == 1) {
            preferencesRepository.completeTutorial()
        }

        preferencesRepository.completeFirstUse()
        preferencesRepository.useTheme(cloudSave.selectedTheme.toLong())
        preferencesRepository.setSquareRadius(cloudSave.squareRadius)
        preferencesRepository.setSquareMultiplier(cloudSave.squareSize)
        preferencesRepository.setCustomLongPressTimeout(cloudSave.touchTiming.toLong())
        preferencesRepository.setQuestionMark(cloudSave.questionMark != 0)
        preferencesRepository.setFlagAssistant(gameAssistance != 0)
        preferencesRepository.setHapticFeedback(hapticFeedback != 0)
        preferencesRepository.setHelp(help != 0)
        preferencesRepository.setSoundEffectsEnabled(soundEffects != 0)
        preferencesRepository.setPremiumFeatures(cloudSave.premiumFeatures != 0)
        preferencesRepository.useControlStyle(ControlStyle.values()[cloudSave.controlStyle])

        cloudSave.stats.mapNotNull {
            try {
                Stats(
                    uid = it["uid"]!!.toInt(),
                    duration = it["duration"]!!.toLong(),
                    mines = it["mines"]!!.toInt(),
                    victory = it["victory"]!!.toInt(),
                    width = it["width"]!!.toInt(),
                    height = it["height"]!!.toInt(),
                    openArea = it["openArea"]!!.toInt(),
                )
            } catch (e: Exception) {
                null
            }
        }.forEach {
            statsRepository.addStats(it)
        }

        preferencesRepository.setMigrateFromCloud(false)
    }
}
