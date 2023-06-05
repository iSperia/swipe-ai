package com.pl00t.swipe_client.services.battle.logic

data class Effect(
    val duration: Int,
    val skin: EffectSkin,
    val increaseDamage: ElementalConfig,
    val increaseResist: ElementalConfig,
    val regeneration: Float
)
