package dev.lucasnlm.antimine

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import dev.lucasnlm.antimine.core.themes.model.AppTheme
import dev.lucasnlm.antimine.core.themes.repository.IThemeRepository
import org.koin.android.ext.android.inject

open class ThematicActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {

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
