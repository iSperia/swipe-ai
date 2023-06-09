package com.pl00t.swipe_client.services.battle.logic.processor.skills

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior

class MeleeAttackSkillBehavior(
    val calculageDamage: (Battle, Character) -> ElementalConfig
) : SkillBehavior() {

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val target = battle.meleeTarget(character) ?: return ProcessResult(emptyList(), battle)
        val damage = calculageDamage(battle, character).let {
            if (lucky) it.copy(
                physical = it.physical * 2f,
                cold = it.cold * 2f,
                fire = it.cold * 2f,
                light = it.light * 2f,
                shock = it.shock * 2f,
                dark = it.dark * 2f
            ) else it
        }
        return battle.dealDamage(character, target, at, damage)
    }
}
