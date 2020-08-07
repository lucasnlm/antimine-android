package dev.lucasnlm.antimine

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasnlm.antimine.core.themes.repository.IThemeRepository
import javax.inject.Inject

open class ThematicActivity : AppCompatActivity {
    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    @Inject
    lateinit var themeRepository: IThemeRepository

    protected open val noActionBar: Boolean = false

    protected val usingThemeId: Long by lazy {
        currentThemeId()
    }

    private fun currentThemeId(): Long = themeRepository.getTheme().id

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

        if (usingThemeId != currentThemeId()) {
            recreate()
        }
    }
}
