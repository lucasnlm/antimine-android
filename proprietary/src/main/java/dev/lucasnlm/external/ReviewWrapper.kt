package dev.lucasnlm.external

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewWrapper : IReviewWrapper {
    override fun startReview(activity: Activity, appPackage: String) {
        val playStoreUri = "market://details?id=$appPackage"
        val playStorePage = "https://play.google.com/store/apps/details?id=$appPackage"
        val manager = ReviewManagerFactory.create(activity)
        manager
            .requestReviewFlow()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (!activity.isFinishing) {
                        manager.launchReviewFlow(activity, it.result)
                    }
                } else if (!activity.isFinishing) {
                    try {
                        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUri)))
                    } catch (e: ActivityNotFoundException) {
                        activity.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(playStorePage)
                            )
                        )
                    }
                }
            }
    }
}
