package com.pl00t.swipe_client.services.battle.logic.processor.skills

import com.pl00t.swipe_client.services.battle.logic.*

fun Battle.dealDamage(
    source: Character,
    target: Character,
    at: Tile,
    damage: ElementalConfig,

): ProcessResult {
    var damage = damage
    val weaknessTiles = source.field.tiles.filter { it.skin == TileSkin.COMMON_WEAKNESS }
    println("${source.skin} weakness: ${weaknessTiles.size}")
    val weakAmount = weaknessTiles.size * 0.025f
    val weakKoef = 1f - weakAmount
    var battle = this
    val events = mutableListOf<BattleEvent>()

    if (weakAmount > 0f) {
        damage = damage.copy(
            physical = damage.physical * weakKoef,
            fire = damage.fire * weakKoef,
            cold = damage.cold * weakKoef,
            light = damage.light * weakKoef,
            dark = damage.dark * weakKoef,
            shock = damage.shock * weakKoef
        )
        var source = source
        battle = battle.updateOrRemoveUnit(source)
    }
    val resist = target.resists
    damage = damage.applyResist(resist)
    val totalDamage = damage.totalDamage()
    val targetAfterDamage = target.copy(health = target.health - totalDamage)


    val icons = damage.iconsIfPositive().toMutableList()
    if (weaknessTiles.isNotEmpty()) {
        icons.add("weak")
    }
    events.add(BattleEvent.UnitPopupEvent(target.id, UnitPopup(
        icons = icons,
        text = totalDamage.toString()
    )))
    events.add(BattleEvent.UnitHealthEvent(target.id, targetAfterDamage.health))
    if (targetAfterDamage.health <= 0) {
        events.add(BattleEvent.UnitDeathEvent(target.id))
    }
    battle = battle.updateOrRemoveUnit(targetAfterDamage)

    return ProcessResult(events, battle)
}
