package dev.lucasnlm.antimine.gdx.events

sealed class GameEvent(
    val id: Int,
) {
    class TouchUpEvent(
        id: Int,
    ) : GameEvent(id)

    class TouchDownEvent(
        id: Int,
    ) : GameEvent(id)
}
