package dev.lucasnlm.antimine.gameover.viewmodel

import android.content.Context
import androidx.annotation.DrawableRes
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.gameover.model.GameResult
import kotlinx.coroutines.flow.flow

class EndGameDialogViewModel(
    private val context: Context,
) : IntentViewModel<EndGameDialogEvent, EndGameDialogState>() {

    private fun List<Int>.safeRandomEmoji(
        @DrawableRes except: Int,
        fallback: Int = R.drawable.emoji_smiling_face_with_sunglasses
    ) = filter { it != except }
        .ifEmpty { listOf(fallback) }
        .random()

    private fun randomVictoryEmoji(except: Int) = listOf(
        R.drawable.emoji_beaming_face_with_smiling_eyes,
        R.drawable.emoji_astonished_face,
        R.drawable.emoji_cowboy_hat_face,
        R.drawable.emoji_face_with_tongue,
        R.drawable.emoji_grimacing_face,
        R.drawable.emoji_grinning_face,
        R.drawable.emoji_grinning_squinting_face,
        R.drawable.emoji_smiling_face_with_sunglasses,
        R.drawable.emoji_squinting_face_with_tongue,
        R.drawable.emoji_hugging_face,
        R.drawable.emoji_partying_face,
        R.drawable.emoji_clapping_hands,
        R.drawable.emoji_triangular_flag,
    ).safeRandomEmoji(except)

    private fun randomNeutralEmoji(@DrawableRes except: Int) = listOf(
        R.drawable.emoji_grimacing_face,
        R.drawable.emoji_smiling_face_with_sunglasses,
        R.drawable.emoji_triangular_flag,
    ).safeRandomEmoji(except)

    private fun randomGameOverEmoji(@DrawableRes except: Int) = listOf(
        R.drawable.emoji_anguished_face,
        R.drawable.emoji_astonished_face,
        R.drawable.emoji_bomb,
        R.drawable.emoji_confounded_face,
        R.drawable.emoji_crying_face,
        R.drawable.emoji_disappointed_face,
        R.drawable.emoji_disguised_face,
        R.drawable.emoji_dizzy_face,
        R.drawable.emoji_downcast_face_with_sweat,
        R.drawable.emoji_exploding_head,
        R.drawable.emoji_face_with_head_bandage,
        R.drawable.emoji_collision,
        R.drawable.emoji_sad_but_relieved_face,
    ).safeRandomEmoji(except)

    private fun messageTo(minesCount: Int, time: Long, gameResult: GameResult): String =
        if (time != 0L) {
            when (gameResult) {
                GameResult.Victory -> context.getString(R.string.generic_win, minesCount, time)
                GameResult.GameOver -> context.getString(R.string.generic_game_over)
                else -> context.getString(R.string.generic_game_over)
            }
        } else {
            context.getString(R.string.generic_game_over)
        }

    override fun initialState() = EndGameDialogState(
        R.drawable.emoji_triangular_flag,
        "",
        "",
        gameResult = GameResult.Completed,
        showContinueButton = false,
        received = 0
    )

    override suspend fun mapEventToState(event: EndGameDialogEvent) = flow {
        if (event is EndGameDialogEvent.BuildCustomEndGame) {
            val state = when (event.gameResult) {
                GameResult.Victory -> {
                    EndGameDialogState(
                        titleEmoji = randomVictoryEmoji(0),
                        title = context.getString(R.string.you_won),
                        message = messageTo(event.rightMines, event.time, event.gameResult),
                        gameResult = event.gameResult,
                        showContinueButton = false,
                        received = event.received
                    )
                }
                GameResult.GameOver -> {
                    EndGameDialogState(
                        titleEmoji = randomGameOverEmoji(0),
                        title = context.getString(R.string.you_lost),
                        message = messageTo(event.rightMines, event.time, event.gameResult),
                        gameResult = event.gameResult,
                        showContinueButton = event.showContinueButton,
                        received = event.received
                    )
                }
                GameResult.Completed -> {
                    EndGameDialogState(
                        titleEmoji = randomNeutralEmoji(0),
                        title = context.getString(R.string.you_finished),
                        message = context.getString(R.string.new_game_request),
                        gameResult = event.gameResult,
                        showContinueButton = false,
                        received = event.received
                    )
                }
            }

            emit(state)
        } else if (event is EndGameDialogEvent.ChangeEmoji) {
            when (event.gameResult) {
                GameResult.Victory -> {
                    emit(state.copy(titleEmoji = randomVictoryEmoji(event.titleEmoji)))
                }
                GameResult.GameOver -> {
                    emit(state.copy(titleEmoji = randomGameOverEmoji(event.titleEmoji)))
                }
                GameResult.Completed -> {
                    emit(state.copy(titleEmoji = randomNeutralEmoji(event.titleEmoji)))
                }
            }
        }
    }
}
