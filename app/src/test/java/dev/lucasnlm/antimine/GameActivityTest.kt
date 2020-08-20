package dev.lucasnlm.antimine

import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withTagKey
import androidx.test.espresso.matcher.ViewMatchers.withText
import dev.lucasnlm.antimine.di.AppModule
import dev.lucasnlm.antimine.di.TestCommonModule
import dev.lucasnlm.antimine.di.TestLevelModule
import dev.lucasnlm.antimine.di.ViewModelModule
import dev.lucasnlm.antimine.level.view.EndGameDialogFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTestRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P], application = TestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class GameActivityTest {
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(AppModule, TestLevelModule, TestCommonModule, ViewModelModule)
    }

    @Test
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
    fun testShowVictoryWhenTapAllSafeAreas() {
        launchActivity<GameActivity>().onActivity { activity ->
            ShadowLooper.runUiThreadTasks()

            // First tap
            activity
                .findViewById<RecyclerView>(R.id.recyclerGrid)
                .findViewHolderForItemId(40).itemView.performClick()

            ShadowLooper.runUiThreadTasks()

            // Tap on safe places
            activity.gameViewModel.field
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
