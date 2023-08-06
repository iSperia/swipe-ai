package com.game7th.swipe.game

import com.game7th.swipe.SbText

data class FrontMonsterAbilityField(
    val title: SbText,
    val value: String
)

data class FrontMonsterAbility(
    val skin: String,
    val title: SbText,
    val description: SbText,
    val fields: List<FrontMonsterAbilityField>
)
