package dev.lucasnlm.antimine.common.level.repository

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import dev.lucasnlm.antimine.common.R

class DrawableRepository {
    private var flag: Drawable? = null
    private var redFlag: Drawable? = null
    private var mineExploded: Drawable? = null
    private var mine: Drawable? = null
    private var mineLow: Drawable? = null
    private var question: Drawable? = null

    fun provideFlagDrawable(context: Context) =
        flag ?: ContextCompat.getDrawable(context, R.drawable.flag).also { flag = it }

    fun provideRedFlagDrawable(context: Context) =
        redFlag ?: ContextCompat.getDrawable(context, R.drawable.red_flag).also { redFlag = it }

    fun provideQuestionDrawable(context: Context) =
        question ?: ContextCompat.getDrawable(context, R.drawable.question).also { question = it }

    fun provideMineExploded(context: Context) =
        mineExploded ?: ContextCompat.getDrawable(
            context,
            R.drawable.mine_exploded
        ).also { mineExploded = it }

    fun provideMine(context: Context) =
        mine ?: ContextCompat.getDrawable(context, R.drawable.mine).also { mine = it }

    fun provideMineLow(context: Context) =
        mineLow ?: ContextCompat.getDrawable(context, R.drawable.mine_low).also { mineLow = it }

    fun free() {
        flag = null
        mineExploded = null
        mine = null
        mineLow = null
        question = null
    }
}
