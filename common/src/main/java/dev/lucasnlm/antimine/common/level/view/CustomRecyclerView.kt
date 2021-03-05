package dev.lucasnlm.antimine.common.level.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        // Disable fling due to internal runtime issues with Instant App
        return false
    }
}
