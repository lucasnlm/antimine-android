package dev.lucasnlm.external

import android.app.Activity

interface IReviewWrapper {
    fun startReview(activity: Activity, appPackage: String)
}
