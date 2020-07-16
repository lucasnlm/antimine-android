package dev.lucasnlm.antimine

import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.launchActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.level.view.EndGameDialogFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@HiltAndroidTest
@UninstallModules(LevelModule::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P], application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class GameActivityTest {
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

            val id = activity.viewModel.field.value!!.first { it.hasMine }.id.toLong()

            // Tap on a mine
            activity.findViewById<RecyclerView>(R.id.recyclerGrid)
                .findViewHolderForItemId(id).itemView.performClick()

            ShadowLooper.idleMainLooper(2, TimeUnit.SECONDS)
            ShadowLooper.runUiThreadTasks()

            val endGame = activity.supportFragmentManager.findFragmentByTag(EndGameDialogFragment.TAG)
            assertNotNull(endGame)
            assertEquals(endGame?.arguments?.get(EndGameDialogFragment.DIALOG_IS_VICTORY), false)
        }
    }

    @Test
    fun testShowVictoryWhenTapAllSafeAreas() {
        launchActivity<GameActivity>().onActivity { activity ->
            ShadowLooper.runUiThreadTasks()

            val mines = activity.viewModel.field.value!!.filter { it.hasMine }.map { it.id.toLong() }.toList()
            val safeAreas = activity.viewModel.field.value!!.filter { !it.hasMine }.map { it.id.toLong() }.toList()

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
