package dev.lucasnlm.antimine.cloud

import dev.lucasnlm.antimine.common.level.database.models.toHashMap
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.external.ICloudStorageManager
import dev.lucasnlm.external.IPlayGamesManager
import dev.lucasnlm.external.model.CloudSave
import kotlinx.coroutines.*

class CloudSaveManagerImpl(
    private val scope: CoroutineScope,
    private val playGamesManager: IPlayGamesManager,
    private val preferencesRepository: IPreferencesRepository,
    private val statsRepository: IStatsRepository,
    private val cloudStorageManager: ICloudStorageManager,
) : CloudSaveManager {
    override fun uploadSave() {
        scope.launch(Dispatchers.IO) {
            getCloudSave()?.let {
                cloudStorageManager.uploadSave(it)
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
                    selectedTheme = preferencesRepository.themeId()?.toInt() ?: -1,
                    selectedSkin = preferencesRepository.skinId().toInt(),
                    touchTiming = preferencesRepository.customLongPressTimeout().toInt(),
                    questionMark = preferencesRepository.useQuestionMark().toInt(),
                    gameAssistance = preferencesRepository.useFlagAssistant().toInt(),
                    help = preferencesRepository.useHelp().toInt(),
                    hapticFeedback = preferencesRepository.useHapticFeedback().toInt(),
                    hapticFeedbackLevel = preferencesRepository.getHapticFeedbackLevel(),
                    soundEffects = preferencesRepository.isSoundEffectsEnabled().toInt(),
                    stats = statsRepository.getAllStats(minId).map { it.toHashMap() },
                    premiumFeatures = preferencesRepository.isPremiumEnabled().toInt(),
                    controlStyle = preferencesRepository.controlStyle().ordinal,
                    openDirectly = preferencesRepository.openGameDirectly().toInt(),
                    doubleClickTimeout = preferencesRepository.getDoubleClickTimeout().toInt(),
                    allowTapNumbers = preferencesRepository.allowTapOnNumbers().toInt(),
                    highlightNumbers = preferencesRepository.dimNumbers().toInt(),
                    leftHanded = 0,
                    dimNumbers = preferencesRepository.dimNumbers().toInt(),
                    timerVisible = preferencesRepository.showTimer().toInt(),
                )
            }
        } catch (e: Exception) {
            return null
        }
    }

    private fun Boolean.toInt() = if (this) 1 else 0
}
