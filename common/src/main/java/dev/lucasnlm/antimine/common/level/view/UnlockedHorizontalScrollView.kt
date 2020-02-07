package dev.lucasnlm.antimine.common.level.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import androidx.recyclerview.widget.RecyclerView

class UnlockedHorizontalScrollView : HorizontalScrollView {
    private var recyclerView: RecyclerView? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    fun setTarget(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean =
        super.onTouchEvent(event) or recyclerView!!.onTouchEvent(event)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean =
        super.onInterceptTouchEvent(event) or recyclerView!!.onInterceptTouchEvent(event)
}
