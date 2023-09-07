package dev.lucasnlm.antimine.ui.ext

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import dev.lucasnlm.antimine.ui.model.AppSkin
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.TopBarAction
import dev.lucasnlm.antimine.ui.repository.ThemeRepository
import org.koin.android.ext.android.inject

/**
 * A class that provides a themed activity.
 */
abstract class ThemedActivity : AppCompatActivity() {
    protected val themeRepository: ThemeRepository by inject()

    protected val usingTheme: AppTheme by lazy { currentTheme() }
    protected val usingSkin: AppSkin by lazy { currentSkin() }

    private fun currentTheme() = themeRepository.getTheme()

    private fun currentSkin() = themeRepository.getSkin()

    private var topBarAction: TopBarAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(usingTheme.theme)
        super.onCreate(savedInstanceState)
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

    @Suppress("DEPRECATION")
    protected fun compatOverridePendingTransition() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overridePendingTransition(0, 0)
        } else {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        }
    }

    override fun onResume() {
        super.onResume()

        if (usingTheme.id != currentTheme().id || usingSkin.id != currentSkin().id) {
            recreate()
            compatOverridePendingTransition()
        }
    }
}
