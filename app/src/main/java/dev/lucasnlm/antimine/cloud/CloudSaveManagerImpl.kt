package dev.lucasnlm.antimine.cloud

import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.core.cloud.CloudSaveManager
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.external.CloudStorageManager
import dev.lucasnlm.external.PlayGamesManager
import dev.lucasnlm.external.model.CloudSave
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        return runCatching {
            val prefs = preferencesRepository
            playGamesManager.playerId()?.let { playerId ->
                CloudSave(
                    playId = playerId,
                    completeTutorial = if (prefs.isTutorialCompleted()) 1 else 0,
                    selectedTheme = prefs.themeId()?.toInt() ?: -1,
                    selectedSkin = prefs.skinId().toInt(),
                    touchTiming = prefs.customLongPressTimeout().toInt(),
                    questionMark = prefs.useQuestionMark().toInt(),
                    gameAssistance = prefs.useFlagAssistant().toInt(),
                    help = prefs.useHelp().toInt(),
                    hapticFeedback = prefs.useHapticFeedback().toInt(),
                    hapticFeedbackLevel = prefs.getHapticFeedbackLevel(),
                    soundEffects = prefs.isSoundEffectsEnabled().toInt(),
                    music = prefs.isMusicEnabled().toInt(),
                    stats = statsRepository.getAllStats().map { it.toHashMap() },
                    premiumFeatures = prefs.isPremiumEnabled().toInt(),
                    controlStyle = prefs.controlStyle().ordinal,
                    openDirectly = prefs.openGameDirectly().toInt(),
                    doubleClickTimeout = prefs.getDoubleClickTimeout().toInt(),
                    allowTapNumbers = prefs.allowTapOnNumbers().toInt(),
                    highlightNumbers = prefs.dimNumbers().toInt(),
                    leftHanded = 0,
                    dimNumbers = prefs.dimNumbers().toInt(),
                    timerVisible = prefs.showTimer().toInt(),
                )
            }
        }.getOrNull()
    }

    private fun Boolean.toInt() = if (this) 1 else 0
}
