package dev.lucasnlm.antimine.common.level.models

sealed class StateUpdate {
    class Single(val index: Int) : StateUpdate()

    object Multiple : StateUpdate()

    object None : StateUpdate()
}
