package dev.lucasnlm.antimine.core.analytics

import android.content.Context
import dev.lucasnlm.antimine.core.analytics.models.Analytics

interface AnalyticsManager {
    fun setup(context: Context, userProperties: Map<String, String>)
    fun sentEvent(event: Analytics)
}
