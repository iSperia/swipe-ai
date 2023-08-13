package com.game7th.swipe

enum class Lang {
    EN, RU
}

data class SbText(
    val en: String,
    val ru: String?,
) {
    fun value(l: Lang) = when (l) {
        Lang.EN -> en
        Lang.RU -> ru ?: en
    }
}
