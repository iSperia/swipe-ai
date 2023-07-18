package com.game7th.swipe.battle

data class Effect(
    val duration: Int,
    val skin: EffectSkin,
    val increaseDamage: ElementalConfig,
    val increaseResist: ElementalConfig,
    val regeneration: Float
)
