package dev.lucasnlm.antimine.playgames.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import dev.lucasnlm.antimine.databinding.ViewPlayGamesButtonBinding

class PlayGamesButtonView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val binding: ViewPlayGamesButtonBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = ViewPlayGamesButtonBinding.inflate(layoutInflater, this, true)
    }
}
