package com.pl00t.swipe_client.services.battle.logic.processor.skills.characters

import com.pl00t.swipe_client.services.battle.MonsterAbilityConfiguration
import com.pl00t.swipe_client.services.battle.floatAttribute
import com.pl00t.swipe_client.services.battle.intAttribute
import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.animateDirectedAoe
import com.pl00t.swipe_client.services.battle.logic.processor.animateMeleeAttack
import com.pl00t.swipe_client.services.battle.logic.processor.skills.AoeSkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.HealBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.MeleeAttackSkillBehavior


class ArborealFangsSkill(config: MonsterAbilityConfiguration) : SkillBehavior() {
    private val basePhysicalDamage = config.attributes.floatAttribute("physDamage")
    private val physPerBody = config.attributes.floatAttribute("physPerBody")

    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = basePhysicalDamage * (1f + character.attributes.body * physPerBody / 100f)
        ElementalConfig(physical = physicalDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.CORRUPTED_DRYAD_ARBOREAL_FANGS)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean) = melee.skillUse(battle, character, at, lucky)

}

class VileSiphonSkill(config: MonsterAbilityConfiguration): SkillBehavior() {

    val physDamage = config.attributes.floatAttribute("physDamage")
    val physPerBody = config.attributes.floatAttribute("physPerBody")
    val heal = config.attributes.floatAttribute("heal")
    val healPerSpirit = config.attributes.floatAttribute("healPerSpirit")

    private val healBehavior = HealBehavior(
        this::getHealTargets,
        this::calculateHeal
    )

    private fun calculateDamage(battle: Battle, character: Character): ElementalConfig {
        return (physDamage * (1f + physPerBody / 100f * character.attributes.body)).let {
            ElementalConfig(physical = it)
        }
    }

    private fun calculateHeal(battle: Battle, character: Character, target: Character): Int {
        return (heal * (1f + healPerSpirit / 100f * character.attributes.spirit)).toInt()
    }

    private fun getHealTargets(battle: Battle, character: Character) = listOf(character)

    private val melee = MeleeAttackSkillBehavior(this::calculateDamage)


    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.CORRUPTED_DRYAD_VILE_SIPHON)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val events = mutableListOf<BattleEvent>()
        var battle = battle
        melee.skillUse(battle, character, at, lucky).let {
            events.addAll(it.events)
            battle = it.battle
        }
        healBehavior.skillUse(battle, character, at, lucky).let {
            events.addAll(it.events)
            battle = it.battle
        }
        return ProcessResult(events, battle)
    }
}

class ShadowedAnnihinlation(config: MonsterAbilityConfiguration): SkillBehavior() {

    private val damage = config.attributes.floatAttribute("damage")
    private val damagePerSpirit = config.attributes.floatAttribute("damagePerSpirit")
    private val tiles = config.attributes.intAttribute("tiles")

    private fun calculateDamage(battle: Battle, character: Character) =
        (damage * (1f + damagePerSpirit / 100f * character.attributes.spirit)).let {
            ElementalConfig(dark = it, physical = it)
        }

    private val aoe = AoeSkillBehavior(this::calculateDamage)

    override fun animationStrategy(battle: Battle, unitId: Int) = animateDirectedAoe(battle, unitId, TileSkin.CORRUPTED_DRYAD_SHADOWED_ANNIHILATION)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val events = mutableListOf<BattleEvent>()
        var battle = battle
        battle.enemies(character).forEach { enemy ->
            var enemy = enemy
            val positions = (0 until 25).shuffled().take(if (lucky) tiles * 2 else tiles)
            positions.forEach { p ->
                events.add(BattleEvent.TileEffectEvent(enemy.id, p % 5, p / 5, TileSkin.CORRUPTED_DRYAD_SHADOWED_ANNIHILATION))
                enemy.field.tiles.filter { it.x == p % 5 && it.y == p / 5 }.forEach { tileToRemove ->
                    events.add(BattleEvent.DestroyTileEvent(enemy.id, tileToRemove.id, tileToRemove.layer))
                }
                battle = battle.updateOrRemoveUnit(enemy.copy(field = enemy.field.copy(
                    tiles = enemy.field.tiles.filter { it.x != p % 5 || it.y != p / 5 }
                )))
                battle.unitById(enemy.id)?.let { enemy = it }
            }
        }
        aoe.skillUse(battle, character, at, lucky).let {
            events.addAll(it.events)
            battle = it.battle
        }
        return ProcessResult(events, battle)
    }
}
