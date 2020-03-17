package dev.lucasnlm.antimine.common.level.data

enum class Mark {
    None,
    Flag,
    Question,
    PurposefulNone;

    fun isFlag(): Boolean = this == Flag

    fun isQuestion(): Boolean = this == Question

    fun isNone(): Boolean = this == None || this == PurposefulNone

    fun isNotNone(): Boolean = this.isNone().not()
}

