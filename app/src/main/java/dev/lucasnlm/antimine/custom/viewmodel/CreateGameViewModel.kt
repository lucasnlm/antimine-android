package dev.lucasnlm.antimine.custom.viewmodel

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import dev.lucasnlm.antimine.DeepLink
import dev.lucasnlm.antimine.common.level.models.Minefield
import dev.lucasnlm.antimine.core.preferences.IPreferencesRepository

class CreateGameViewModel @ViewModelInject constructor(
    application: Application,
    private val preferencesRepository: IPreferencesRepository
) : AndroidViewModel(application) {
    fun updateCustomGameMode(minefield: Minefield) {
        preferencesRepository.updateCustomGameMode(minefield)
    }

    fun startCustomGame() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(DeepLink.CUSTOM_NEW_GAME)
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            addFlags(FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(FLAG_ACTIVITY_NO_ANIMATION)
        }
        getApplication<Application>().startActivity(intent)
    }
}
