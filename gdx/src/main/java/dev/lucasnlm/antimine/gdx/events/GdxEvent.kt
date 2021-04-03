package dev.lucasnlm.antimine.gdx.events

sealed class GdxEvent(
    val id: Int,
) {
    class TouchUpEvent(
        id: Int,
    ) : GdxEvent(id)

    class TouchDownEvent(
        id: Int,
    ) : GdxEvent(id)
}
