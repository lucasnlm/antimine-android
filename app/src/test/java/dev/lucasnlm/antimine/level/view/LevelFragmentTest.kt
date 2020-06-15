package dev.lucasnlm.antimine.level.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dev.lucasnlm.antimine.GameActivity
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.di.AppModule
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@UninstallModules(LevelModule::class, AppModule::class)
@Config(sdk = [16, 28], application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
@HiltAndroidTest
class LevelFragmentTest {

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Test
    fun testShowGameOverWhenTapAMine() {
        launchActivity<GameActivity>().onActivity { activity ->
            ShadowLooper.runUiThreadTasks()

            // First tap
            activity.findViewById<RecyclerView>(R.id.recyclerGrid)
                .findViewHolderForItemId(40).itemView.performClick()

            ShadowLooper.runUiThreadTasks()

            // Tap on a mine
            activity.findViewById<RecyclerView>(R.id.recyclerGrid)
                .findViewHolderForItemId(26).itemView.performClick()

            ShadowLooper.idleMainLooper(2, TimeUnit.SECONDS)
            ShadowLooper.runUiThreadTasks()

            val endGame = activity.supportFragmentManager.findFragmentByTag(EndGameDialogFragment.TAG)
            assertNotNull(endGame)
            assertEquals(endGame?.arguments?.get(EndGameDialogFragment.DIALOG_IS_VICTORY), false)
        }
    }

    @Test
    fun testShowVictoryWhenTapAllSafeAreas() {
        val mines = sequenceOf(4, 9, 15, 26, 47, 53, 68, 71, 75)
        val safeAreas = (0 until 81).filterNot { mines.contains(it) }.map { it.toLong() }

        launchActivity<GameActivity>().onActivity { activity ->
            ShadowLooper.runUiThreadTasks()

            // First tap
            activity.findViewById<RecyclerView>(R.id.recyclerGrid)
                .findViewHolderForItemId(40).itemView.performClick()

            ShadowLooper.runUiThreadTasks()

            // Tap on safe places
            safeAreas.forEach { safeArea ->
                activity.findViewById<RecyclerView>(R.id.recyclerGrid)
                    .findViewHolderForItemId(safeArea).itemView.performClick()
                ShadowLooper.runUiThreadTasks()
            }

            ShadowLooper.idleMainLooper(2, TimeUnit.SECONDS)
            ShadowLooper.runUiThreadTasks()

            val endGame = activity.supportFragmentManager.findFragmentByTag(EndGameDialogFragment.TAG)
            assertNotNull(endGame)
            assertEquals(endGame?.arguments?.get(EndGameDialogFragment.DIALOG_IS_VICTORY), true)
        }
    }
}
