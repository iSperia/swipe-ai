package com.pl00t.swipe_client.services.battle.logic.processor.skills.characters

import com.pl00t.swipe_client.services.battle.logic.Battle
import com.pl00t.swipe_client.services.battle.logic.Character
import com.pl00t.swipe_client.services.battle.logic.ElementalConfig
import com.pl00t.swipe_client.services.battle.logic.TileSkin
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.animateMeleeAttack
import com.pl00t.swipe_client.services.battle.logic.processor.animateSelfStatic
import com.pl00t.swipe_client.services.battle.logic.processor.skills.MeleeAttackSkillBehavior

class PrimalAssaultBehaviour: SkillBehavior() {
    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = 5f * (1f + character.attributes.body * 0.1f)
        ElementalConfig(physical = physicalDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THORNSTALKER_PRIMAL_ASSAULT)

    override fun skillUse(battle: Battle, character: Character, lucky: Boolean) = melee.skillUse(battle, character, lucky)
}

class ResilentGrowth: SkillBehavior() {
    override fun animationStrategy(battle: Battle, unitId: Int) = animateSelfStatic(battle, unitId, TileSkin.THORNSTALKER_RESILIENT_GROWTH)
}

class VenomousBarrageBehavior: SkillBehavior() {
    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THORNSTALKER_VENOMOUS_BARRAGE)
}
