package dev.lucasnlm.antimine.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import org.koin.android.ext.android.inject

abstract class ThematicActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {

    private val themeRepository: IThemeRepository by inject()

    protected open val noActionBar: Boolean = false

    protected val usingTheme: AppTheme by lazy {
        currentTheme()
    }

    private fun currentTheme() = themeRepository.getTheme()

    override fun onCreate(savedInstanceState: Bundle?) {
        themeRepository.getCustomTheme()?.let {
            if (noActionBar) {
                setTheme(it.themeNoActionBar)
            } else {
                setTheme(it.theme)
            }
        }
        super.onCreate(savedInstanceState)
        supportActionBar?.elevation = 0.0f

        window.decorView.setBackgroundColor(
            themeRepository.getTheme().palette.background.toAndroidColor()
        )
    }

    override fun onResume() {
        super.onResume()

        if (usingTheme.id != currentTheme().id) {
            finish()
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}
