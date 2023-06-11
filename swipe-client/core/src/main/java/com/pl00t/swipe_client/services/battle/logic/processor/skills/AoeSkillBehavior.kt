package com.pl00t.swipe_client.services.battle.logic.processor.skills

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior

class AoeSkillBehavior(
    val calculateDamage: (Battle, Character) -> ElementalConfig
) : SkillBehavior() {

    override fun skillUse(battle: Battle, character: Character, lucky: Boolean): ProcessResult {
        val enemies = battle.enemies(character)
        val events = mutableListOf<BattleEvent>()

        val damage = calculateDamage(battle, character).let {
            if (lucky) it.copy(
                physical = it.physical * 2f,
                cold = it.cold * 2f,
                fire = it.cold * 2f,
                light = it.light * 2f,
                shock = it.shock * 2f,
                dark = it.dark * 2f
            ) else it
        }

        var updatedBattle = battle

        enemies.forEach { target ->
            val resist = target.resists
            val damageAfterResist = damage.applyResist(resist)
            val totalDamage = damageAfterResist.totalDamage()
            val targetAfterDamage = target.copy(health = target.health - totalDamage)

            events.add(BattleEvent.UnitPopupEvent(target.id, UnitPopup(
                icons = damageAfterResist.iconsIfPositive(),
                text = totalDamage.toString()
            )))
            events.add(BattleEvent.UnitHealthEvent(target.id, targetAfterDamage.health))
            if (targetAfterDamage.health <= 0) {
                events.add(BattleEvent.UnitDeathEvent(target.id))
            }
            updatedBattle = battle.updateOrRemoveUnit(targetAfterDamage)
        }

        return ProcessResult(events, updatedBattle)
    }

}
