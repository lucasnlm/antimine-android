package dev.lucasnlm.antimine.cloud

import dev.lucasnlm.antimine.common.level.database.models.toHashMap
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.external.CloudStorageManager
import dev.lucasnlm.external.PlayGamesManager
import dev.lucasnlm.external.model.CloudSave
import kotlinx.coroutines.*

class CloudSaveManagerImpl(
    private val scope: CoroutineScope,
    private val playGamesManager: PlayGamesManager,
    private val preferencesRepository: PreferencesRepository,
    private val statsRepository: StatsRepository,
    private val cloudStorageManager: CloudStorageManager,
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
                    music = preferencesRepository.isMusicEnabled().toInt(),
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
