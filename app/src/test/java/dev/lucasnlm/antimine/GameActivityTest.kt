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
import org.junit.Ignore
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
    @Ignore("Dagger hilt issue")
    fun testShowGameOverWhenTapAMine() {
        launchActivity<GameActivity>().onActivity { activity ->
            ShadowLooper.runUiThreadTasks()

            // First tap
            activity.findViewById<RecyclerView>(R.id.recyclerGrid)
                .findViewHolderForItemId(40).itemView.performClick()

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

            val idWithMine = 4L

            // Tap on a mine
            activity.findViewById<RecyclerView>(R.id.recyclerGrid)
                .findViewHolderForItemId(idWithMine).itemView.performClick()

            ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

            val endGame = activity.supportFragmentManager.findFragmentByTag(EndGameDialogFragment.TAG)
            assertNotNull(endGame)
            assertEquals(false, endGame?.arguments?.get(EndGameDialogFragment.DIALOG_IS_VICTORY))
        }
    }

    @Test
    @Ignore("Dagger hilt issue")
    fun testShowVictoryWhenTapAllSafeAreas() {
        launchActivity<GameActivity>().onActivity { activity ->
            ShadowLooper.runUiThreadTasks()

            // First tap
            activity
                .findViewById<RecyclerView>(R.id.recyclerGrid)
                .findViewHolderForItemId(40).itemView.performClick()

            ShadowLooper.runUiThreadTasks()

            // Tap on safe places
            activity.viewModel.field
                .value!!
                .filter { !it.hasMine && it.isCovered }
                .forEach {
                    if (it.isCovered) {
                        activity
                            .findViewById<RecyclerView>(R.id.recyclerGrid)
                            .findViewHolderForItemId(it.id.toLong())
                            .itemView
                            .performClick()
                    }
                    ShadowLooper.runUiThreadTasks()
                }

            ShadowLooper.idleMainLooper(2, TimeUnit.SECONDS)
            ShadowLooper.runUiThreadTasks()

            val endGame = activity.supportFragmentManager.findFragmentByTag(EndGameDialogFragment.TAG)
            assertNotNull(endGame)
            assertEquals(true, endGame?.arguments?.get(EndGameDialogFragment.DIALOG_IS_VICTORY))
        }
    }
}
