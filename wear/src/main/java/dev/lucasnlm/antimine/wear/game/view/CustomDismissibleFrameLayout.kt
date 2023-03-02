package dev.lucasnlm.antimine.wear.game.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

class CustomDismissibleFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var childFragment: WeakReference<Fragment>? = null

    fun setChildFragment(fragment: Fragment) {
        childFragment = WeakReference(fragment)
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        val child = childFragment?.get()
        return child?.view?.onTouchEvent(event) ?: true
    }
}
