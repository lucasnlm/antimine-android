package dev.lucasnlm.antimine.ui.ext

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.TopBarAction
import dev.lucasnlm.antimine.ui.repository.IThemeRepository
import org.koin.android.ext.android.inject

abstract class ThematicActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {
    protected val themeRepository: IThemeRepository by inject()

    protected val usingTheme: AppTheme by lazy {
        currentTheme()
    }

    protected val usingSkin: AppSkin by lazy {
        currentSkin()
    }

    private fun currentTheme() = themeRepository.getTheme()

    private fun currentSkin() = themeRepository.getSkin()

    private var topBarAction: TopBarAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        themeRepository.getCustomTheme()?.let {
            setTheme(it.theme)
        }

        super.onCreate(savedInstanceState)

        window.decorView.setBackgroundColor(
            themeRepository.getTheme().palette.background.toAndroidColor(),
        )
    }

    protected fun bindToolbar(toolbar: MaterialToolbar?) {
        toolbar?.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener {
                finish()
            }
        }
    }

    fun setTopBarAction(topBarAction: TopBarAction?) {
        this.topBarAction = topBarAction
        invalidateMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            topBarAction?.let { action ->
                menu.add(action.name)?.apply {
                    setIcon(action.icon)
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    setOnMenuItemClickListener {
                        action.action()
                        true
                    }
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()

        if (usingTheme.id != currentTheme().id) {
            recreate()
            overridePendingTransition(0, 0)
        } else if (usingSkin.id != currentSkin().id) {
            recreate()
            overridePendingTransition(0, 0)
        }
    }
}
