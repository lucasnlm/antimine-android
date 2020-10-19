package dev.lucasnlm.antimine.cloud

import dev.lucasnlm.antimine.common.level.database.models.toHashMap
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository
import dev.lucasnlm.external.ICloudStorageManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.model.CloudSave
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloudSaveManager(
    private val playGamesManager: IPlayGamesManager,
    private val preferencesRepository: IPreferencesRepository,
    private val statsRepository: IStatsRepository,
    private val cloudStorageManager: ICloudStorageManager,
) {
    fun uploadSave() {
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
                    questionMark = if (preferencesRepository.useQuestionMark()) 1 else 0,
                    gameAssistance = if (preferencesRepository.useFlagAssistant()) 1 else 0,
                    help = if (preferencesRepository.useHelp()) 1 else 0,
                    hapticFeedback = if (preferencesRepository.useHapticFeedback()) 1 else 0,
                    soundEffects = if (preferencesRepository.isSoundEffectsEnabled()) 1 else 0,
                    stats = statsRepository.getAllStats(minId).map { it.toHashMap() },
                    premiumFeatures = if (preferencesRepository.isPremiumEnabled()) 1 else 0,
                    controlStyle = preferencesRepository.controlStyle().ordinal,
                )
            }
        } catch (e: Exception) {
            return null
        }
    }
}
