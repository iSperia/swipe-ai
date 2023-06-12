package com.pl00t.swipe_client.services.battle.logic.processor.skills.characters

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.processor.*
import com.pl00t.swipe_client.services.battle.logic.processor.skills.AoeSkillBehavior
import com.pl00t.swipe_client.services.battle.logic.processor.skills.MeleeAttackSkillBehavior
import kotlin.math.min

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

    override fun skillUse(battle: Battle, character: Character, lucky: Boolean): ProcessResult {
        val numTiles = if (lucky) 4 else 2
        val filledPositions = character.field.tiles.filter { it.layer == 0 }.map { it.x + it.y * 5 }.toSet()
        val positions = (0 until 25).filter { !filledPositions.contains(it) }.shuffled().take(numTiles)
        var tileId = character.field.maxTileId
        var field = character.field
        val events = mutableListOf<BattleEvent>()
        positions.forEach { position ->
            val x = position % 5
            val y = position / 5
            val tile = Tile(
                skin = TileSkin.VALERIAN_SIGIL_OF_RENEWAL_BG,
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
                unitId = character.id,
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
        val character = character.copy(field = field)
        val battle = battle.updateOrRemoveUnit(character)
        return ProcessResult(events, battle)
    }
}

class SigilOfRenewalBackgroundBehavior : SkillBehavior() {

    init {
        println("SIGIL BG init")
    }
    override fun afterTileUsed(battle: Battle, character: Character, self: Tile, target: Tile): ProcessResult {
        if (self.x == target.x && self.y == target.y) {
            //we are sigil under the usage stuff
            //first of all, we heal the stuff
            val healAmount = (5f * (1f + 0.1f * character.attributes.mind)).toInt()
            val healthAfterHeal = min(character.maxHealth, character.health + healAmount)
            var unit = character.copy(health = healthAfterHeal)
            unit = unit.copy(field = unit.field.copy(tiles = unit.field.tiles.filterNot { it.id == self.id }))
            var battle = battle.updateOrRemoveUnit(unit)
            val events = mutableListOf<BattleEvent>()
            events.add(BattleEvent.DestroyTileEvent(character.id, self.id, self.layer))
            events.add(BattleEvent.UnitHealthEvent(unit.id, healthAfterHeal))
            events.add(BattleEvent.UnitPopupEvent(unit.id, UnitPopup(listOf("heal"), healAmount.toString())))

            return ProcessResult(events, battle)
        } else {
            return ProcessResult(emptyList(), battle)
        }
    }
}

class DivineConvergenceBehavior : SkillBehavior() {

    override fun skillUse(battle: Battle, character: Character, lucky: Boolean): ProcessResult {
        val tilesToExplode = character.field.tiles.filter { it.skin == TileSkin.VALERIAN_SIGIL_OF_RENEWAL_BG }
        val events = mutableListOf<BattleEvent>()
        val healAmount = (5f * tilesToExplode.size * (1f + (character.attributes.mind + character.attributes.body) * 0.1f)).toInt()
        val damage = 5f * tilesToExplode.size * (1f + character.attributes.spirit * 0.1f)

        val healthAfterHeal = min(character.maxHealth, character.health + healAmount)
        var character = character.copy(health = healthAfterHeal)
        events.add(BattleEvent.UnitHealthEvent(character.id, healthAfterHeal))
        events.add(BattleEvent.UnitPopupEvent(character.id, UnitPopup(listOf("heal"), healAmount.toString())))
        tilesToExplode.forEach { tile ->
            character = character.removeTile(tile.id)
            events.add(BattleEvent.DestroyTileEvent(character.id, tile.id, tile.layer))
        }
        var battle = battle.updateOrRemoveUnit(character)

        val aoe = AoeSkillBehavior { battle, character -> ElementalConfig(light = damage) }
        val aoeResult = aoe.skillUse(battle, character, false)
        battle = aoeResult.battle
        events.addAll(aoeResult.events)

        return ProcessResult(listOf(BattleEvent.UltimateEvent(character.id, TileSkin.VALERIAN_DIVINE_CONVERGENCE, events)), battle)
    }
}
