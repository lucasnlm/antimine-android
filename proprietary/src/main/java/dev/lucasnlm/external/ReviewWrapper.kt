package dev.lucasnlm.external

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewWrapper : IReviewWrapper {
    override fun startReviewPage(activity: Activity, appPackage: String) {
        if (!activity.isFinishing) {
            val playStoreUri = "market://details?id=$appPackage"
            val playStorePage = "https://play.google.com/store/apps/details?id=$appPackage"

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

    override fun startInAppReview(activity: Activity) {
        ReviewManagerFactory.create(activity).run {
            requestReviewFlow()
                .addOnCompleteListener {
                    if (it.isSuccessful && !activity.isFinishing) {
                        launchReviewFlow(activity, it.result)
                    }
                }
        }
    }
}
