package dev.lucasnlm.antimine.main.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.viewModelScope
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.IStatsRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.viewmodel.StatelessViewModel
import dev.lucasnlm.antimine.main.MainActivity
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.tutorial.TutorialActivity
import dev.lucasnlm.external.ICloudStorageManager
import dev.lucasnlm.external.model.CloudSave
import kotlinx.coroutines.launch

class MainViewModel(
    private val application: Application,
    private val preferencesRepository: IPreferencesRepository,
    private val statsRepository: IStatsRepository,
    private val saveCloudStorageManager: ICloudStorageManager,
) : StatelessViewModel<MainEvent>() {
    override fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.ContinueGameEvent -> continueGame()
            is MainEvent.StartNewGameEvent -> continueGame(event.difficulty)
            is MainEvent.ShowCustomDifficultyDialogEvent -> showCustomDifficultyDialogEvent()
            is MainEvent.StartTutorialEvent -> startTutorial()
            is MainEvent.GoToSettingsPageEvent -> goToSettingsPageEvent()
            is MainEvent.GoToMainPageEvent -> goToMainPageEvent()
            is MainEvent.ShowControlsEvent -> showControlsEvent()
            is MainEvent.ShowGooglePlayGamesEvent -> showGooglePlayGames()
            is MainEvent.FetchCloudSave -> fetchCloudSave(event.playGamesId)
            else -> {
            }
        }
    }

    private fun showCustomDifficultyDialogEvent() {
        sendSideEffect(MainEvent.ShowCustomDifficultyDialogEvent)
    }

    private fun goToSettingsPageEvent() {
        sendSideEffect(MainEvent.GoToSettingsPageEvent)
    }

    private fun goToMainPageEvent() {
        sendSideEffect(MainEvent.GoToMainPageEvent)
    }

    private fun showControlsEvent() {
        sendSideEffect(MainEvent.ShowControlsEvent)
    }

    private fun showGooglePlayGames() {
        sendSideEffect(MainEvent.ShowGooglePlayGamesEvent)
    }

    private fun fetchCloudSave(playGamesId: String) {
        viewModelScope.launch {
            saveCloudStorageManager.getSave(playGamesId)?.let { cloudSave ->
                loadCloudSave(cloudSave)
                sendSideEffect(MainEvent.Recreate)
            }
        }
    }

    private suspend fun loadCloudSave(cloudSave: CloudSave) = with(cloudSave) {
        with(preferencesRepository) {
            setCompleteTutorial(completeTutorial == 1)
            completeFirstUse()
            useTheme(selectedTheme.toLong())
            setSquareRadius(squareRadius)
            setSquareSize(squareSize)
            setCustomLongPressTimeout(touchTiming.toLong())
            setQuestionMark(questionMark != 0)
            setFlagAssistant(gameAssistance != 0)
            setHapticFeedback(hapticFeedback != 0)
            setHapticFeedbackLevel(hapticFeedbackLevel)
            setHelp(help != 0)
            setAllowTapOnNumbers(allowTapNumbers != 0)
            setSoundEffectsEnabled(soundEffects != 0)
            setPremiumFeatures(premiumFeatures != 0)
            useControlStyle(ControlStyle.values()[controlStyle])
            setOpenGameDirectly(openDirectly != 0)
            setUnlockedThemes(unlockedThemes)
            setSquareDivider(squareDivider)
            setDoubleClickTimeout(doubleClickTimeout.toLong())
            setLeftHandedMode(leftHanded != 0)
            setDimNumbers(dimNumbers != 0)
            setTimerVisible(timerVisible != 0)
        }

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
        }.distinctBy {
            it.uid
        }.also {
            try {
                statsRepository.addAllStats(it)
            } catch (e: Exception) {
                Log.e(MainActivity.TAG, "Fail to insert stats on DB")
            }
        }
    }

    private fun continueGame(difficulty: Difficulty? = null) {
        val context = application.applicationContext
        val intent = Intent(context, GameActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            difficulty?.let {
                val bundle = Bundle().apply {
                    putSerializable(GameActivity.DIFFICULTY, it)
                }
                putExtras(bundle)
            }
        }
        context.startActivity(intent)
    }

    private fun startTutorial() {
        val context = application.applicationContext
        val intent = Intent(context, TutorialActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}
