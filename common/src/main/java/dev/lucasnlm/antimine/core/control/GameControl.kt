package dev.lucasnlm.antimine.core.control

/**
 * Possible action response to an user action.
 */
enum class ActionResponse {
    OpenTile,
    SwitchMark,
    HighlightNeighbors,
    OpenNeighbors,
}

/**
 * [Actions] links an [ActionResponse] to an user action.
 */
data class Actions(
    val singleClick: ActionResponse?,
    val doubleClick: ActionResponse?,
    val longPress: ActionResponse?
)

/**
 * These are the current available game control styles.
 * Check [GameControl] to details.
 */
enum class ControlStyle {
    Standard,
    DoubleClick,
    FastFlag
}

/**
 * [GameControl] will map an user action (from [Actions]) to an [ActionResponse].
 * This is necessary because same users rather that single click open the tile, other that it flags the tile.
 */
sealed class GameControl(
    val id: ControlStyle,
    val onCovered: Actions,
    val onOpen: Actions
) {
    object Standard : GameControl(
        id = ControlStyle.Standard,
        onCovered = Actions(
            singleClick = ActionResponse.OpenTile,
            longPress = ActionResponse.SwitchMark,
            doubleClick = null
        ),
        onOpen = Actions(
            singleClick = ActionResponse.HighlightNeighbors,
            longPress = ActionResponse.OpenNeighbors,
            doubleClick = null
        )
    )

    object FastFlag : GameControl(
        id = ControlStyle.FastFlag,
        onCovered = Actions(
            singleClick = ActionResponse.SwitchMark,
            longPress = ActionResponse.OpenTile,
            doubleClick = null
        ),
        onOpen = Actions(
            singleClick = ActionResponse.OpenNeighbors,
            longPress = ActionResponse.HighlightNeighbors,
            doubleClick = null
        )
    )

    object DoubleClick : GameControl(
        id = ControlStyle.DoubleClick,
        onCovered = Actions(
            singleClick = ActionResponse.SwitchMark,
            longPress = null,
            doubleClick = ActionResponse.OpenTile
        ),
        onOpen = Actions(
            singleClick = ActionResponse.HighlightNeighbors,
            longPress = null,
            doubleClick = ActionResponse.OpenNeighbors
        )
    )

    companion object {
        fun fromControlType(controlStyle: ControlStyle): GameControl {
            return when (controlStyle) {
                ControlStyle.Standard -> Standard
                ControlStyle.DoubleClick -> DoubleClick
                ControlStyle.FastFlag -> FastFlag
            }
        }
    }
}

/**
 * A data class used to make feedback or analytics to an user action.
 */
data class ActionFeedback(
    val actionResponse: ActionResponse?,
    val index: Int,
    val multipleChanges: Boolean
)
