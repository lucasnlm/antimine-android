package dev.lucasnlm.external.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import dev.lucasnlm.antimine.BuildConfig

class AdPlaceHolderView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val adView: AdView = AdView(context).apply {
        adSize = AdSize.BANNER
        adUnitId = "ca-app-pub-3940256099942544/6300978111"
    }

    init {
        addView(adView)

        if (BuildConfig.DEBUG) {
            addView(View(context).apply {
                setBackgroundResource(android.R.color.holo_blue_bright)
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        50f,
                        context.resources.displayMetrics
                    ).toInt()
                )
            })
        }

        setOnClickListener {
            adView.performClick()
            this.visibility = View.GONE
        }
    }

    fun loadAd() {
        adView.loadAd(AdRequest.Builder().build())

        if (!adView.isLoading) {
            this.visibility = View.GONE
        }
    }
}
