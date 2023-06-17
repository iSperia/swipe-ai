package com.pl00t.swipe_client.services.battle.logic.processor.skills.characters

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.animateDirectedAoe
import com.pl00t.swipe_client.services.battle.logic.processor.animateMeleeAttack
import com.pl00t.swipe_client.services.battle.logic.processor.skills.AoeSkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.HealBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.MeleeAttackSkillBehavior


class ArborealFangsSkill : SkillBehavior() {
    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = 6f * (1f + character.attributes.body * 0.1f)
        ElementalConfig(physical = physicalDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.CORRUPTED_DRYAD_ARBOREAL_FANGS)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean) = melee.skillUse(battle, character, at, lucky)

}

class VileSiphonSkill: SkillBehavior() {

    private fun calcualteDamage(battle: Battle, character: Character): ElementalConfig {
        return (2f * (1f + 0.1f * character.attributes.body)).let {
            ElementalConfig(physical = it)
        }
    }

    private fun calculateHeal(battle: Battle, character: Character, target: Character): Int {
        return (2f * (1f + 0.1f * character.attributes.spirit)).toInt()
    }

    private fun getHealTargets(battle: Battle, character: Character) = listOf(character)

    private val melee = MeleeAttackSkillBehavior(this::calcualteDamage)

    private val heal = HealBehavior(
        this::getHealTargets,
        this::calculateHeal
    )

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.CORRUPTED_DRYAD_VILE_SIPHON)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val events = mutableListOf<BattleEvent>()
        var battle = battle
        melee.skillUse(battle, character, at, lucky).let {
            events.addAll(it.events)
            battle = it.battle
        }
        heal.skillUse(battle, character, at, lucky).let {
            events.addAll(it.events)
            battle = it.battle
        }
        return ProcessResult(events, battle)
    }
}

class ShadowedAnnihinlation: SkillBehavior() {

    private fun calculateDamage(battle: Battle, character: Character) =
        (2f * (1f + 0.1f * character.attributes.spirit)).let {
            ElementalConfig(dark = it, physical = it)
        }

    private val aoe = AoeSkillBehavior(this::calculateDamage)

    override fun animationStrategy(battle: Battle, unitId: Int) = animateDirectedAoe(battle, unitId, TileSkin.CORRUPTED_DRYAD_SHADOWED_ANNIHILATION)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val events = mutableListOf<BattleEvent>()
        var battle = battle
        battle.enemies(character).forEach { enemy ->
            var enemy = enemy
            val positions = (0 until 25).shuffled().take(5)
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
