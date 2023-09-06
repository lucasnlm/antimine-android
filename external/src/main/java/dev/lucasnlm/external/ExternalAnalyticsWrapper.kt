package dev.lucasnlm.external

import android.content.Context

interface ExternalAnalyticsWrapper {
    fun setup(
        context: Context,
        properties: Map<String, String>,
    )

    fun sendEvent(
        name: String,
        content: Map<String, String>,
    )
}
