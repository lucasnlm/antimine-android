package dev.lucasnlm.external

import android.content.Context
import dev.lucasnlm.antimine.core.models.Analytics

/**
 * Wrapper for external analytics libraries.
 */
interface AnalyticsManager {
    /**
     * Setup the analytics library.
     */
    fun setup(
        context: Context,
        properties: Map<String, String>,
    )

    /**
     * Send an event to the analytics library.
     */
    fun sentEvent(event: Analytics)
}
