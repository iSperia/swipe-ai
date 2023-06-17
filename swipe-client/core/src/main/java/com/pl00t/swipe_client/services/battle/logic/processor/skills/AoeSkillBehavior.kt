package com.pl00t.swipe_client.services.battle.logic.processor.skills

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior

class AoeSkillBehavior(
    val calculateDamage: (Battle, Character) -> ElementalConfig
) : SkillBehavior() {

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
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
            val r = battle.dealDamage(character, target, at, damage)
            updatedBattle = r.battle
            events.addAll(r.events)
        }

        return ProcessResult(events, updatedBattle)
    }

}
