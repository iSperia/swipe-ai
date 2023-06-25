package com.pl00t.swipe_client.services.battle.logic.processor.skills.characters

import com.pl00t.swipe_client.services.battle.MonsterAbilityConfiguration
import com.pl00t.swipe_client.services.battle.floatAttribute
import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.animateMeleeAttack
import com.pl00t.swipe_client.services.battle.logic.processor.animateSelfStatic
import com.pl00t.swipe_client.services.battle.logic.processor.skills.HealBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.MeleeAttackSkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.InflictPoisonBehavior

class PrimalAssaultBehaviour(config: MonsterAbilityConfiguration): SkillBehavior() {

    private val physDamage = config.attributes.floatAttribute("physDamage")
    private val physPerBody = config.attributes.floatAttribute("physPerBody")

    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = physDamage * (1f + character.attributes.body * physPerBody / 100f)
        ElementalConfig(physical = physicalDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THORNSTALKER_PRIMAL_ASSAULT)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean) = melee.skillUse(battle, character, at, lucky)
}

class ResilentGrowth(config: MonsterAbilityConfiguration): SkillBehavior() {

    private val heal = config.attributes.floatAttribute("heal")
    private val healPerSpirit = config.attributes.floatAttribute("healPerSpirit")

    private val healBehavior = HealBehavior(
        this::getHealTargets,
        this::calculateHeal
    )

    private fun calculateHeal(battle: Battle, character: Character, target: Character): Int {
        return (heal * (1f + healPerSpirit / 100f * character.attributes.spirit)).toInt()
    }

    private fun getHealTargets(battle: Battle, character: Character) = listOf(character)


    override fun animationStrategy(battle: Battle, unitId: Int) = animateSelfStatic(battle, unitId, TileSkin.THORNSTALKER_RESILIENT_GROWTH)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val events = mutableListOf<BattleEvent>()
        var battle = battle
        healBehavior.skillUse(battle, character, at, lucky).let {
            events.addAll(it.events)
            battle = battle
        }
        return ProcessResult(events, battle)
    }
}

class VenomousBarrageBehavior(config: MonsterAbilityConfiguration): SkillBehavior() {

    private val poisonDamage = config.attributes.floatAttribute("poisonDamage")
    private val poisonPerSpirit = config.attributes.floatAttribute("poisonPerSpirit")

    val poison = InflictPoisonBehavior(
        getTargets = this::getTargets,
        getAmount = this::getAmount
    )

    private fun getAmount(battle: Battle, character: Character, target: Character): Int {
        val amount = poisonDamage * (1f + poisonPerSpirit * character.attributes.spirit / 100f)
        return amount.toInt()
    }

    private fun getTargets(battle: Battle, character: Character): List<Character> = listOfNotNull(battle.meleeTarget(character))

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THORNSTALKER_VENOMOUS_BARRAGE)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val poisonResult = poison.skillUse(battle, character, at, lucky)
        var battle = poisonResult.battle
        val events = mutableListOf<BattleEvent>()
        events.addAll(poisonResult.events)
        return ProcessResult(events, battle)
    }
}
