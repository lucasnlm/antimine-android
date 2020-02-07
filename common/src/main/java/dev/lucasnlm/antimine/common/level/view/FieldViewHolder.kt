package dev.lucasnlm.antimine.common.level.view

import android.os.Build
import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.lucasnlm.antimine.common.R

class FieldViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val areaView: AreaView = view.findViewById(R.id.area)

    init {
        view.isFocusable = false
        areaView.isFocusable = true
        areaView.setOnKeyListener { _, keyCode, keyEvent ->
            var handled = false

            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                when (keyEvent.action) {
                    KeyEvent.ACTION_DOWN -> {
                        longPressAt = System.currentTimeMillis()
                        handled = true
                    }
                    KeyEvent.ACTION_UP -> {
                        if (System.currentTimeMillis() - longPressAt > 300L) {
                            view.performLongClick()
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                                view.callOnClick()
                            } else {
                                view.performClick()
                            }
                        }
                        handled = true
                    }
                }
            }

            handled
        }
    }

    companion object {
        var longPressAt: Long = 0L
    }
}
