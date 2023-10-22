package dev.lucasnlm.antimine.preferences.models

/**
 * Possible action response to an user action.
 */
enum class Action {
    OpenTile,
    SwitchMark,
    QuestionMark,
    OpenNeighbors,
    OpenOrMark,
}

/**
 * [Actions] links an [Action] to an user action.
 */
data class Actions(
    val singleClick: Action?,
    val doubleClick: Action?,
    val longPress: Action?,
)

/**
 * These are the current available game control styles.
 * Check [GameControl] to details.
 */
enum class ControlStyle {
    Standard,
    DoubleClick,
    FastFlag,
    DoubleClickInverted,
    SwitchMarkOpen,
}

/**
 * [GameControl] will map an user action (from [Actions]) to an [Action].
 * This is necessary because same users rather that single click open the tile, other that it flags the tile.
 */
sealed class GameControl(
    val id: ControlStyle,
    val onCovered: Actions,
    val onUncovered: Actions,
) {
    data object Standard : GameControl(
        id = ControlStyle.Standard,
        onCovered =
            Actions(
                singleClick = Action.OpenTile,
                longPress = Action.SwitchMark,
                doubleClick = null,
            ),
        onUncovered =
            Actions(
                singleClick = null,
                longPress = Action.OpenNeighbors,
                doubleClick = null,
            ),
    )

    data object FastFlag : GameControl(
        id = ControlStyle.FastFlag,
        onCovered =
            Actions(
                singleClick = Action.SwitchMark,
                longPress = Action.OpenTile,
                doubleClick = null,
            ),
        onUncovered =
            Actions(
                singleClick = Action.OpenNeighbors,
                longPress = null,
                doubleClick = null,
            ),
    )

    data object DoubleClick : GameControl(
        id = ControlStyle.DoubleClick,
        onCovered =
            Actions(
                singleClick = Action.SwitchMark,
                longPress = null,
                doubleClick = Action.OpenTile,
            ),
        onUncovered =
            Actions(
                singleClick = Action.OpenNeighbors,
                longPress = null,
                doubleClick = null,
            ),
    )

    data object DoubleClickInverted : GameControl(
        id = ControlStyle.DoubleClickInverted,
        onCovered =
            Actions(
                singleClick = Action.OpenTile,
                longPress = null,
                doubleClick = Action.SwitchMark,
            ),
        onUncovered =
            Actions(
                singleClick = Action.OpenNeighbors,
                longPress = null,
                doubleClick = null,
            ),
    )

    data object SwitchMarkOpen : GameControl(
        id = ControlStyle.SwitchMarkOpen,
        onCovered =
            Actions(
                singleClick = Action.OpenOrMark,
                longPress = null,
                doubleClick = null,
            ),
        onUncovered =
            Actions(
                singleClick = Action.OpenNeighbors,
                longPress = null,
                doubleClick = null,
            ),
    )

    companion object {
        fun fromControlType(controlStyle: ControlStyle): GameControl {
            return when (controlStyle) {
                ControlStyle.Standard -> Standard
                ControlStyle.DoubleClick -> DoubleClick
                ControlStyle.FastFlag -> FastFlag
                ControlStyle.DoubleClickInverted -> DoubleClickInverted
                ControlStyle.SwitchMarkOpen -> SwitchMarkOpen
            }
        }
    }
}
