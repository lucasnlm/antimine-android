package dev.lucasnlm.antimine.main

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.control.ControlDialogFragment
import dev.lucasnlm.antimine.custom.CustomLevelDialogFragment
import dev.lucasnlm.antimine.main.view.MainPageAdapter
import dev.lucasnlm.antimine.main.viewmodel.MainEvent
import dev.lucasnlm.antimine.main.viewmodel.MainViewModel
import dev.lucasnlm.antimine.playgames.PlayGamesDialogFragment
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.splash.SplashActivity
import dev.lucasnlm.antimine.ui.ThematicActivity
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.external.IPlayGamesManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator3
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ThematicActivity(R.layout.activity_main) {
    private val viewModel: MainViewModel by viewModel()
    private val playGamesManager: IPlayGamesManager by inject()
    private val preferencesRepository: IPreferencesRepository by inject()

    private lateinit var viewPager: ViewPager2
    private var currentBackgroundColor: Int =
        usingTheme.palette.background.toAndroidColor()

    private val pageListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val fromColor = currentBackgroundColor
            val toColor = usingTheme.palette.accent.toAndroidColor((position + 1) * 64)

            ValueAnimator.ofArgb(fromColor, toColor).apply {
                duration = 250L
                addUpdateListener {
                    currentBackgroundColor = it.animatedValue as Int
                    root.background.setTint(currentBackgroundColor)
                }
                start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewPager = findViewById<ViewPager2>(R.id.pager).apply {
            adapter = MainPageAdapter(
                fragmentActivity = this@MainActivity,
                fragments = listOf(
                    MainPageFragment(),
                    SettingsPageFragment(),
                )
            )
            currentItem = 0
            registerOnPageChangeCallback(pageListener)
        }

        currentBackgroundColor =
            usingTheme.palette.accent.toAndroidColor((1 + viewPager.currentItem) * 64)

        findViewById<CircleIndicator3>(R.id.circle_indicator).apply {
            setViewPager(pager)
            tintIndicator(usingTheme.palette.accent.toAndroidColor())
        }

        lifecycleScope.launchWhenCreated {
            viewModel
                .observeSideEffects()
                .collect(::handleSideEffects)
        }

        launchGooglePlayGames()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager.unregisterOnPageChangeCallback(pageListener)
    }

    private fun handleSideEffects(event: MainEvent) {
        when (event) {
            is MainEvent.ShowCustomDifficultyDialogEvent -> {
                showCustomLevelDialog()
            }
            is MainEvent.GoToMainPageEvent -> {
                viewPager.setCurrentItem(0, true)
            }
            is MainEvent.GoToSettingsPageEvent -> {
                viewPager.setCurrentItem(1, true)
            }
            is MainEvent.ShowControlsEvent -> {
                showControlDialog()
            }
            is MainEvent.ShowGooglePlayGamesEvent -> {
                showGooglePlayGames()
            }
            is MainEvent.Recreate -> {
                finish()
                startActivity(Intent(this, SplashActivity::class.java))
                overridePendingTransition(0, 0)
            }
            else -> {}
        }
    }

    private fun showCustomLevelDialog() {
        if (supportFragmentManager.findFragmentByTag(CustomLevelDialogFragment.TAG) == null) {
            CustomLevelDialogFragment().apply {
                show(supportFragmentManager, CustomLevelDialogFragment.TAG)
            }
        }
    }

    private fun showControlDialog() {
        if (supportFragmentManager.findFragmentByTag(CustomLevelDialogFragment.TAG) == null) {
            ControlDialogFragment().apply {
                show(supportFragmentManager, ControlDialogFragment.TAG)
            }
        }
    }

    private fun showGooglePlayGames() {
        if (playGamesManager.isLogged()) {
            if (supportFragmentManager.findFragmentByTag(PlayGamesDialogFragment.TAG) == null) {
                PlayGamesDialogFragment().show(supportFragmentManager, PlayGamesDialogFragment.TAG)
            }
        } else {
            playGamesManager.getLoginIntent()?.let {
                ActivityCompat.startActivityForResult(this, it, RC_GOOGLE_PLAY, null)
            }
        }
    }

    private fun launchGooglePlayGames() {
        if (playGamesManager.hasGooglePlayGames()) {
            lifecycleScope.launchWhenCreated {
                var logged: Boolean

                try {
                    withContext(Dispatchers.IO) {
                        logged = playGamesManager.silentLogin()
                        if (logged) { refreshUserId() }
                        playGamesManager.showPlayPopUp(this@MainActivity)
                    }
                } catch (e: Exception) {
                    logged = false
                    Log.e(SplashActivity.TAG, "Failed silent login", e)
                }

                if (!logged) {
                    try {
                        playGamesManager.getLoginIntent()?.let {
                            ActivityCompat.startActivityForResult(
                                this@MainActivity, it,
                                RC_GOOGLE_PLAY, null
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(SplashActivity.TAG, "User not logged or doesn't have Play Games", e)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_PLAY) {
            playGamesManager.handleLoginResult(data)
            lifecycleScope.launch {
                refreshUserId()
            }
        }
    }

    private suspend fun refreshUserId() {
        withContext(Dispatchers.Default) {
            val lastId = preferencesRepository.userId()
            val newId = playGamesManager.playerId()

            if (lastId != newId && newId != null) {
                preferencesRepository.setUserId(newId)

                withContext(Dispatchers.Main) {
                    migrateDateAndRecreate()
                }
            }
        }
    }

    private fun migrateDateAndRecreate() {
        lifecycleScope.launchWhenCreated {
            if (!isFinishing) {
                preferencesRepository.userId()?.let {
                    viewModel.sendEvent(MainEvent.FetchCloudSave(it))
                }
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    companion object {
        private const val RC_GOOGLE_PLAY = 6
    }
}
