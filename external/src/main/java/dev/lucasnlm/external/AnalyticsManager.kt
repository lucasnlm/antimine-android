package dev.lucasnlm.external

import android.content.Context
import dev.lucasnlm.antimine.core.models.Analytics

interface AnalyticsManager {
    fun setup(
        context: Context,
        properties: Map<String, String>,
    )

    fun sentEvent(event: Analytics)
}
