package dev.lucasnlm.antimine.common.level.widget

import androidx.recyclerview.widget.RecyclerView

class FreeGridLayoutManager(
    private val maxColumnCount: Int
) : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.MATCH_PARENT
        )
    }

}
