package dev.lucasnlm.antimine.tutorial

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.tutorial.databinding.TutorialActivityBinding
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.external.AnalyticsManager
import org.koin.android.ext.android.inject

class TutorialActivity : ThemedActivity() {
    private val preferencesRepository: PreferencesRepository by inject()
    private val analyticsManager: AnalyticsManager by inject()
    private val audioManager: GameAudioManager by inject()

    private val binding: TutorialActivityBinding by lazy {
        TutorialActivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        preferencesRepository.setTutorialDialog(false)
        analyticsManager.sentEvent(Analytics.OpenTutorial)

        bindToolbar(binding.toolbar)

        binding.playGame.setOnClickListener {
            finish()

            audioManager.playClickSound()

            val deeplink = Uri.parse(NEW_GAME_DEEPLINK)
            val intent =
                Intent(Intent.ACTION_VIEW, deeplink).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            startActivity(intent)
        }
    }

    companion object {
        const val NEW_GAME_DEEPLINK = "app://antimine/game?difficulty=beginner"
    }
}
