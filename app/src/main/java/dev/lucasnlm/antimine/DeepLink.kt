package dev.lucasnlm.antimine

sealed class DeepLink {
    companion object {
        const val DEFAULT_SCHEME = "antimine"

        const val DEEP_LINK_NEW_GAME_HOST = "//new-game/"
        const val DEEP_LINK_RETRY_HOST = "//retry-game/"
        const val DEEP_LINK_LOAD_GAME_HOST = "//load-game/"
        const val DEEP_LINK_BEGINNER = "beginner"
        const val DEEP_LINK_INTERMEDIATE = "intermediate"
        const val DEEP_LINK_EXPERT = "expert"
        const val DEEP_LINK_STANDARD = "standard"
    }
}
