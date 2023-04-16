package dev.lucasnlm.antimine.common.level.models

import dev.lucasnlm.antimine.preferences.models.Action

data class ActionCompleted(
    val action: Action,
    val amount: Int,
)
