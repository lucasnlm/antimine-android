package dev.lucasnlm.antimine.core.models

enum class Mark {
    None,
    Flag,
    Question,
    PurposefulNone,
    ;

    fun isFlag(): Boolean = this == Flag

    fun isQuestion(): Boolean = this == Question

    fun isPureNone(): Boolean = this == None

    fun isNone(): Boolean = this == None || this == PurposefulNone

    fun isNotNone(): Boolean = this.isNone().not()
}
