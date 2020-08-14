package dev.lucasnlm.antimine.level.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dev.lucasnlm.antimine.R

class EngGameDialogViewModel : ViewModel() {

    private fun List<Int>.safeRandomEmoji(
        except: Int? = null,
        fallback: Int = R.drawable.emoji_smiling_face_with_sunglasses
    ) = this.filter { it != except }
            .ifEmpty { listOf(fallback) }
            .random()

    fun randomVictoryEmoji(except: Int? = null) = listOf(
        R.drawable.emoji_beaming_face_with_smiling_eyes,
        R.drawable.emoji_astonished_face,
        R.drawable.emoji_cowboy_hat_face,
        R.drawable.emoji_face_with_tongue,
        R.drawable.emoji_grimacing_face,
        R.drawable.emoji_grinning_face,
        R.drawable.emoji_grinning_squinting_face,
        R.drawable.emoji_smiling_face_with_sunglasses,
        R.drawable.emoji_squinting_face_with_tongue,
        R.drawable.emoji_partying_face,
        R.drawable.emoji_clapping_hands,
        R.drawable.emoji_triangular_flag
    ).safeRandomEmoji(except)

    fun randomNeutralEmoji(except: Int? = null) = listOf(
        R.drawable.emoji_grimacing_face,
        R.drawable.emoji_smiling_face_with_sunglasses,
        R.drawable.emoji_triangular_flag
    ).safeRandomEmoji(except)

    fun randomGameOverEmoji(except: Int? = null) = listOf(
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
        R.drawable.emoji_face_with_symbols_on_mouth,
        R.drawable.emoji_collision,
        R.drawable.emoji_sad_but_relieved_face

    ).safeRandomEmoji(except)

    fun messageTo(context: Context, time: Long, isVictory: Boolean): String =
        if (time != 0L) {
            when {
                isVictory -> context.getString(R.string.game_over_desc_4, time)
                else -> context.getString(R.string.game_over_desc_1)
            }
        } else {
            context.getString(R.string.game_over_desc_1)
        }
}
