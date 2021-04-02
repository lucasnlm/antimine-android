package dev.lucasnlm.antimine.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import org.koin.android.ext.android.inject
import java.util.Locale

abstract class ThematicActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {
    private val themeRepository: IThemeRepository by inject()
    private val preferencesRepository: IPreferencesRepository by inject()

    protected val usingTheme: AppTheme by lazy {
        currentTheme()
    }

    private val usingPreferredLanguage: String? by lazy {
        currentPreferredLanguage()
    }

    private fun currentTheme() = themeRepository.getTheme()

    private fun currentPreferredLanguage() = preferencesRepository.getPreferredLocale()

    override fun onCreate(savedInstanceState: Bundle?) {
        themeRepository.getCustomTheme()?.let {
            setTheme(it.theme)
        }

        preferencesRepository.getPreferredLocale()?.let {
            updateActivityLanguage(it)
        }

        super.onCreate(savedInstanceState)

        window.decorView.setBackgroundColor(
            themeRepository.getTheme().palette.background.toAndroidColor()
        )
    }

    override fun onResume() {
        super.onResume()

        if (usingTheme.id != currentTheme().id || usingPreferredLanguage != currentPreferredLanguage()) {
            recreate()
            overridePendingTransition(0, 0)
        }
    }

    private fun updateActivityLanguage(localeName: String) {
        val localeStrList = localeName.split("-")
        val locale = Locale(localeStrList.first(), localeStrList.last())
        Locale.setDefault(locale)
        resources?.apply {
            val config = resources.configuration.apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }

            updateConfiguration(config, displayMetrics)
        }
    }
}
