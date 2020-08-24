package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

class BillingManager(
    private val context: Context
) : IBillingManager {

    override fun start(unlockAppListener: UnlockAppListener) {
        // Empty
    }

    override fun isEnabled(): Boolean = false

    override suspend fun charge(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_LINK))
        activity.startActivity(intent)
    }

    companion object {
        const val DONATE_LINK =
            "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=lucasnunesk%40gmail.com&item_name=" +
                "With+your+help,+we%5C%27ll+be+able+to+implement+new+features+and+keep+our+" +
                "project+active.&currency_code=USD&source=url"
    }
}
