package com.pl00t.swipe_client.services.battle.logic.processor.skills.characters

import com.pl00t.swipe_client.services.battle.MonsterAbilityConfiguration
import com.pl00t.swipe_client.services.battle.floatAttribute
import com.pl00t.swipe_client.services.battle.intAttribute
import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.SkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.animateMeleeAttack
import com.pl00t.swipe_client.services.battle.logic.processor.skills.HealBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.MeleeAttackSkillBehavior

class ViciousPincers(config: MonsterAbilityConfiguration) : SkillBehavior() {

    private val physDamage = config.attributes.floatAttribute("physDamage")
    private val physPerBody = config.attributes.floatAttribute("physPerBody")
    private val darkDamage = config.attributes.floatAttribute("darkDamage")
    private val darkPerSpirit = config.attributes.floatAttribute("darkPerSpirit")

    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = physDamage* (1f + character.attributes.body * physPerBody / 100f)
        val darkDamage = darkDamage * (1f + character.attributes.spirit * darkPerSpirit / 100f)
        ElementalConfig(physical = physicalDamage, dark = darkDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THORNED_CRAWLER_VICIOUS_PINCERS)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean) = melee.skillUse(battle, character, at, lucky)

}

class LeechingShadows(config: MonsterAbilityConfiguration) : SkillBehavior() {

    private val darkDamage = config.attributes.floatAttribute("darkDamage")
    private val darkPerSpirit = config.attributes.floatAttribute("darkPerSpirit")
    private val heal = config.attributes.floatAttribute("heal")
    private val healPerSpirit = config.attributes.floatAttribute("healPerSpirit")

    private val healBehavior = HealBehavior(
        this::getHealTargets,
        this::calculateHeal
    )

    private fun calcualteDamage(battle: Battle, character: Character): ElementalConfig {
        return (darkDamage * (1f + darkPerSpirit / 100f * character.attributes.spirit)).let {
            ElementalConfig(dark = it)
        }
    }

    private fun calculateHeal(battle: Battle, character: Character, target: Character): Int {
        return (heal * (1f + healPerSpirit / 100f * character.attributes.spirit)).toInt()
    }

    private fun getHealTargets(battle: Battle, character: Character) = listOf(character)

    private val melee = MeleeAttackSkillBehavior(this::calcualteDamage)

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THORNED_CRAWLER_LEECHING_SHADOWS)

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

class DebiliatingStrike(config: MonsterAbilityConfiguration) : SkillBehavior() {

    private val physDamage = config.attributes.floatAttribute("physDamage")
    private val physPerBody = config.attributes.floatAttribute("physPerBody")
    private val weaknessTiles = config.attributes.intAttribute("weaknessTiles")

    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = physDamage * (1f + character.attributes.body * physPerBody / 100f)
        ElementalConfig(physical = physicalDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THORNED_CRAWLER_VICIOUS_PINCERS)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val target = battle.meleeTarget(character)
        val meleeResult = melee.skillUse(battle, character, at, lucky)

        if (target == null) return meleeResult

        val events = mutableListOf<BattleEvent>()
        var battle = meleeResult.battle
        events.addAll(meleeResult.events)

        val numTiles = if (lucky) weaknessTiles * 2 else weaknessTiles
        val filledPositions = target.field.tiles.filter { it.layer == 0 }.map { it.x + it.y * 5 }.toSet()
        val positions = (0 until 25).filter { !filledPositions.contains(it) }.shuffled().take(numTiles)
        var tileId = target.field.maxTileId
        var field = target.field
        positions.forEach { position ->
            val x = position % 5
            val y = position / 5
            val tile = Tile(
                skin = TileSkin.COMMON_WEAKNESS,
                progress = 0,
                maxProgress = 0,
                x = x,
                y = y,
                id = tileId++,
                layer = 0,
                mobility = 0,
                type = TileType.BACKGROUND
            )
            field = field.copy(tiles = field.tiles + tile, maxTileId = tileId)
            events.add(BattleEvent.CreateTileEvent(
                unitId = target.id,
                id = tile.id,
                x = tile.x,
                y = tile.y,
                skin = tile.skin,
                stack = tile.progress,
                maxStack = tile.maxProgress,
                layer = tile.layer,
                type = tile.type
            ))
        }
        val targetUpdated = target.copy(field = field)

        battle = battle.updateOrRemoveUnit(targetUpdated)
        return ProcessResult(events, battle)
    }
}
