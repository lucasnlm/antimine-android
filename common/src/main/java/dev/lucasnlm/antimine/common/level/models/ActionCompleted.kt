package dev.lucasnlm.antimine.common.level.models

import dev.lucasnlm.antimine.preferences.models.Action

/**
 * This class represents an action that was completed.
 * @property action The action that was completed.
 * @property amount The amount of times the action was completed.
 */
data class ActionCompleted(
    val action: Action,
    val amount: Int,
)
