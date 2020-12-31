package dev.lucasnlm.antimine.cloud

import dev.lucasnlm.antimine.common.level.database.models.toHashMap
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.external.ICloudStorageManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.model.CloudSave
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloudSaveManagerImpl(
    private val playGamesManager: IPlayGamesManager,
    private val preferencesRepository: IPreferencesRepository,
    private val statsRepository: IStatsRepository,
    private val cloudStorageManager: ICloudStorageManager,
) : CloudSaveManager {
    override fun uploadSave() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                getCloudSave()?.let {
                    cloudStorageManager.uploadSave(it)
                }
            }
        }
    }

    private suspend fun getCloudSave(): CloudSave? {
        try {
            val minId = preferencesRepository.getStatsBase()
            return playGamesManager.playerId()?.let { playerId ->
                CloudSave(
                    playId = playerId,
                    completeTutorial = if (preferencesRepository.isTutorialCompleted()) 1 else 0,
                    selectedTheme = if (preferencesRepository.isPremiumEnabled()) 3.coerceAtLeast(
                        preferencesRepository.themeId().toInt()
                    ) else 0,
                    squareRadius = preferencesRepository.squareRadius(),
                    squareSize = preferencesRepository.squareSizeMultiplier(),
                    touchTiming = preferencesRepository.customLongPressTimeout().toInt(),
                    questionMark = preferencesRepository.useQuestionMark().toInt(),
                    gameAssistance = preferencesRepository.useFlagAssistant().toInt(),
                    help = preferencesRepository.useHelp().toInt(),
                    hapticFeedback = preferencesRepository.useHapticFeedback().toInt(),
                    soundEffects = preferencesRepository.isSoundEffectsEnabled().toInt(),
                    stats = statsRepository.getAllStats(minId).map { it.toHashMap() },
                    premiumFeatures = preferencesRepository.isPremiumEnabled().toInt(),
                    controlStyle = preferencesRepository.controlStyle().ordinal,
                    noGuessing = preferencesRepository.useNoGuessingAlgorithm().toInt(),
                )
            }
        } catch (e: Exception) {
            return null
        }
    }

    private fun Boolean.toInt() = if (this) 1 else 0
}
