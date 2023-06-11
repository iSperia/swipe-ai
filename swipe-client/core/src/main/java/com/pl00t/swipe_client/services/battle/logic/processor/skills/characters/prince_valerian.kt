package com.pl00t.swipe_client.services.battle.logic.processor.skills.characters

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.*
import com.pl00t.swipe_client.services.battle.logic.processor.skills.AoeSkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.MeleeAttackSkillBehavior

class RadiantStrikeBehaviour : SkillBehavior() {
    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = 10f * (1f + character.attributes.body * 0.1f)
        ElementalConfig(physical = physicalDamage)
    }
    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.VALERIAN_RADIANT_STRIKE)

    override fun skillUse(battle: Battle, character: Character, lucky: Boolean) = melee.skillUse(battle, character, lucky)
}

class LuminousBeamBehaviour : SkillBehavior() {
    private val aoe = AoeSkillBehavior { battle, character ->
        val lightDamage = 5f * (1f + character.attributes.spirit * 0.1f)
        ElementalConfig(light = lightDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateDirectedAoe(battle, unitId, TileSkin.VALERIAN_LUMINOUS_BEAM)

    override fun skillUse(battle: Battle, character: Character, lucky: Boolean) = aoe.skillUse(battle, character, lucky)
}

class SigilOfRenewalBehavior : SkillBehavior() {
    override fun animationStrategy(battle: Battle, unitId: Int) = animateSelfStatic(battle, unitId, TileSkin.VALERIAN_SIGIL_OF_RENEWAL)
}
