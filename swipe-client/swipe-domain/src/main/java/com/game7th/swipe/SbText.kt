package com.game7th.swipe

enum class Language {
    EN, RU
}

data class SbText(
    val en: String,
    val ru: String?,
) {
    fun value(l: Language) = when (l) {
        Language.EN -> en
        Language.RU -> ru ?: en
    }
}
