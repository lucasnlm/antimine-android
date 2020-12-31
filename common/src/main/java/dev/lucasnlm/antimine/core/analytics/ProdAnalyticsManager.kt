package dev.lucasnlm.antimine.core.analytics

import android.content.Context
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.external.IAnalyticsManager
import dev.lucasnlm.external.IExternalAnalyticsWrapper

class ProdAnalyticsManager(
    private val analyticsWrapper: IExternalAnalyticsWrapper,
) : IAnalyticsManager {
    override fun setup(context: Context, properties: Map<String, String>) {
        analyticsWrapper.setup(context, properties)
    }

    override fun sentEvent(event: Analytics) {
        analyticsWrapper.sendEvent(event.name, event.extra)
    }
}
