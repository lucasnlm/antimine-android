package dev.lucasnlm.antimine.tutorial

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.tutorial.databinding.TutorialActivityBinding
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.external.IAnalyticsManager
import org.koin.android.ext.android.inject

class TutorialActivity : ThemedActivity() {
    private lateinit var binding: TutorialActivityBinding
    private val preferencesRepository: IPreferencesRepository by inject()
    private val analyticsManager: IAnalyticsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TutorialActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesRepository.setTutorialDialog(false)
        analyticsManager.sentEvent(Analytics.OpenTutorial)

        bindToolbar(binding.toolbar)

        binding.playGame.setOnClickListener {
            finish()

            val deeplink = Uri.parse(NEW_GAME_DEEPLINK)
            val intent = Intent(Intent.ACTION_VIEW, deeplink).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }
    }

    companion object {
        const val NEW_GAME_DEEPLINK = "app://antimine/game?difficulty=beginner"
    }
}
