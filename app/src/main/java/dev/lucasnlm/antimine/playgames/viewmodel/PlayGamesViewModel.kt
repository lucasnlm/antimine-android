package dev.lucasnlm.antimine.playgames.viewmodel

import android.app.Activity
import android.content.Context
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.viewmodel.StatelessViewModel
import dev.lucasnlm.antimine.playgames.model.PlayGamesItem
import dev.lucasnlm.external.IPlayGamesManager

class PlayGamesViewModel(
    private val context: Context,
    private val playGamesManager: IPlayGamesManager
) : StatelessViewModel<PlayGamesEvent>() {

    val playGamesItems = listOf(
        PlayGamesItem(0, R.drawable.games_achievements, R.string.achievements, PlayGamesEvent.OpenAchievements),
        PlayGamesItem(1, R.drawable.games_leaderboards, R.string.leaderboards, PlayGamesEvent.OpenLeaderboards)
    )

    fun openAchievements(activity: Activity) {
        playGamesManager.openAchievements(activity)
    }

    fun openLeaderboards(activity: Activity) {
        playGamesManager.openLeaderboards(activity)
    }
}
