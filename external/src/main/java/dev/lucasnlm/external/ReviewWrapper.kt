package dev.lucasnlm.external

import android.app.Activity

interface ReviewWrapper {
    fun startReviewPage(
        activity: Activity,
        appPackage: String,
    )

    fun startInAppReview(activity: Activity)
}
