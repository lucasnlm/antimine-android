package dev.lucasnlm.external

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory
import dev.lucasnlm.antimine.BuildConfig

class ReviewWrapper : IReviewWrapper {
    override fun startReview(activity: Activity) {
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
                        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URI)))
                    } catch (e: ActivityNotFoundException) {
                        activity.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(PLAY_STORE_PAGE)
                            )
                        )
                    }
                }
            }
    }

    companion object {
        private const val PACKAGE_NAME = BuildConfig.LIBRARY_PACKAGE_NAME
        private const val PLAY_STORE_URI = "market://details?id=$PACKAGE_NAME"
        private const val PLAY_STORE_PAGE = "https://play.google.com/store/apps/details?id=$PACKAGE_NAME"
    }
}
