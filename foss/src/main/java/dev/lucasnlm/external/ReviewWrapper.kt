package dev.lucasnlm.external

import android.app.Activity

class ReviewWrapper : IReviewWrapper {
    override fun startReview(activity: Activity) {
        // There's not review on FOSS build.
    }
}
