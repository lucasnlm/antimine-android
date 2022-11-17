package dev.lucasnlm.antimine.ui.ext

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import org.koin.android.ext.android.inject

abstract class ThematicActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {
    private val themeRepository: IThemeRepository by inject()

    protected val usingTheme: AppTheme by lazy {
        currentTheme()
    }

    private fun currentTheme() = themeRepository.getTheme()

    override fun onCreate(savedInstanceState: Bundle?) {
        themeRepository.getCustomTheme()?.let {
            setTheme(it.theme)
        }

        super.onCreate(savedInstanceState)

        window.decorView.setBackgroundColor(
            themeRepository.getTheme().palette.background.toAndroidColor(),
        )
    }

    override fun onResume() {
        super.onResume()

        if (usingTheme.id != currentTheme().id) {
            recreate()
            overridePendingTransition(0, 0)
        }
    }
}
