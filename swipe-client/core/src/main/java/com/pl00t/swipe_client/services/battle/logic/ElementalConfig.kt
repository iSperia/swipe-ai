package com.pl00t.swipe_client.services.battle.logic

data class ElementalConfig(
    val physical: Float = 0f,
    val cold: Float = 0f,
    val fire: Float = 0f,
    val shock: Float = 0f,
    val light: Float = 0f,
    val dark: Float = 0f,
) {
    fun applyResist(resist: ElementalConfig) = ElementalConfig(
        physical = (this.physical * (1f - resist.physical)).normalize0(),
        cold = (this.cold * (1f - resist.cold)).normalize0(),
        light = (this.light * (1f - resist.light)).normalize0(),
        fire = (this.fire * (1f - resist.fire)).normalize0(),
        shock = (this.shock * (1f - resist.shock)).normalize0(),
        dark = (this.dark * (1f - resist.dark)).normalize0(),
    )

    fun totalDamage() = (physical + cold + fire + shock + light + dark).toInt()

    fun iconsIfPositive() = listOfNotNull(
        if (physical > 0f) "physical" else null,
        if (cold > 0f) "cold" else null,
        if (fire > 0f) "fire" else null,
        if (shock > 0f) "shock" else null,
        if (light > 0f) "light" else null,
        if (dark > 0f) "dark" else null
    )
}

fun Float.normalize0() = if (this < 0f) 0f else this
