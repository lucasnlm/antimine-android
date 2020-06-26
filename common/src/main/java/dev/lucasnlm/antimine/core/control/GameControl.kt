package dev.lucasnlm.antimine.core.control

enum class Action {
    OpenTile,
    SwitchMark,
    HighlightNeighbors,
    OpenNeighbors,
}

data class Actions(
    val singleClick: Action?,
    val doubleClick: Action?,
    val longPress: Action?
)

sealed class GameControl(
    val onCovered: Actions,
    val onOpen: Actions
) {
    object Standard : GameControl(
        onCovered = Actions(
            singleClick = Action.OpenTile,
            longPress = Action.SwitchMark,
            doubleClick = null
        ),
        onOpen = Actions(
            singleClick = Action.HighlightNeighbors,
            longPress = Action.OpenNeighbors,
            doubleClick = null
        )
    )
}

data class ActionFeedback(
    val action: Action?,
    val index: Int,
    val multipleChanges: Boolean
)
