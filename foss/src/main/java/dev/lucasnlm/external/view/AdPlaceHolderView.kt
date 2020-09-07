package dev.lucasnlm.external.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class AdPlaceHolderView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    init {
        visibility = GONE
    }

    fun loadAd() { }
}
