package dev.lucasnlm.external

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewWrapperImpl : ReviewWrapper {
    override fun startReviewPage(
        activity: Activity,
        appPackage: String,
    ) {
        if (!activity.isFinishing) {
            val playStoreUri = "market://details?id=$appPackage"
            val playStorePage = "https://play.google.com/store/apps/details?id=$appPackage"

            runCatching {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUri)))
            }.onFailure {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(playStorePage),
                    ),
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
