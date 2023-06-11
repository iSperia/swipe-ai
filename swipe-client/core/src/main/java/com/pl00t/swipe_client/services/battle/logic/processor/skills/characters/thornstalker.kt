package com.pl00t.swipe_client.services.battle.logic.processor.skills.characters

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.animateMeleeAttack
import com.pl00t.swipe_client.services.battle.logic.processor.animateSelfStatic
import com.pl00t.swipe_client.services.battle.logic.processor.skills.MeleeAttackSkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.InflictPoisonBehavior

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

    val poison = InflictPoisonBehavior(
        getTargets = this::getTargets,
        getAmount = this::getAmount
    )

    private fun getAmount(battle: Battle, character: Character, target: Character): Int {
        val amount = 1f * (1f + 0.1f * character.attributes.spirit)
        return amount.toInt()
    }

    private fun getTargets(battle: Battle, character: Character): List<Character> = listOfNotNull(battle.meleeTarget(character))

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THORNSTALKER_VENOMOUS_BARRAGE)

    override fun skillUse(battle: Battle, character: Character, lucky: Boolean): ProcessResult {
        val poisonResult = poison.skillUse(battle, character, lucky)
        var battle = poisonResult.battle
        val events = mutableListOf<BattleEvent>()
        events.addAll(poisonResult.events)
        return ProcessResult(events, battle)
    }
}
