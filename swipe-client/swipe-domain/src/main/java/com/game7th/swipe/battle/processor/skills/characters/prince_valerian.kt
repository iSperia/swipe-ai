package com.game7th.swipe.battle.processor.skills.characters

import com.game7th.swipe.battle.*
import com.game7th.swipe.battle.processor.SkillBehavior
import com.game7th.swipe.battle.processor.animateDirectedAoe
import com.game7th.swipe.battle.processor.animateMeleeAttack
import com.game7th.swipe.battle.processor.animateSelfStatic
import com.game7th.swipe.battle.processor.skills.AoeSkillBehavior
import com.game7th.swipe.battle.processor.skills.MeleeAttackSkillBehavior
import com.game7th.swipe.game.SbDisplayTileType
import kotlin.math.min

class RadiantStrikeBehaviour(config: SbMonsterAbilityConfiguration) : SkillBehavior() {

    private val physDamage = config.attributes.floatAttribute("physDamage")
    private val physPerBody = config.attributes.floatAttribute("physPerBody")

    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = physDamage * (1f + character.attributes.body * physPerBody / 100f)
        ElementalConfig(physical = physicalDamage)
    }
    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.VALERIAN_RADIANT_STRIKE)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean) = melee.skillUse(battle, character, at, lucky)
}

class LuminousBeamBehaviour(config: SbMonsterAbilityConfiguration) : SkillBehavior() {

    private val lightDamage = config.attributes.floatAttribute("lightDamage")
    private val lightPerSpirit = config.attributes.floatAttribute("lightPerSpirit")

    private val aoe = AoeSkillBehavior { battle, character ->
        val lightDamage = lightDamage * (1f + character.attributes.spirit * lightPerSpirit / 100f)
        ElementalConfig(light = lightDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateDirectedAoe(battle, unitId, TileSkin.VALERIAN_LUMINOUS_BEAM)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean) = aoe.skillUse(battle, character, at, lucky)
}

class SigilOfRenewalBehavior(config: SbMonsterAbilityConfiguration) : SkillBehavior() {

    private val numTiles = config.attributes.intAttribute("numTiles")

    override fun animationStrategy(battle: Battle, unitId: Int) = animateSelfStatic(battle, unitId, TileSkin.VALERIAN_SIGIL_OF_RENEWAL)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val numTiles = if (lucky) this.numTiles * 2 else this.numTiles
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
                type = SbDisplayTileType.BACKGROUND
            )
            field = field.copy(tiles = field.tiles + tile, maxTileId = tileId)
            events.add(
                BattleEvent.CreateTileEvent(
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

class SigilOfRenewalBackgroundBehavior(config: SbMonsterAbilityConfiguration) : SkillBehavior() {

    private val healPerSigil = config.attributes.floatAttribute("healPerSigil")
    private val healPerSpirit = config.attributes.floatAttribute("healPerSigil")

    override fun afterTileUsed(battle: Battle, character: Character, self: Tile, target: Tile): ProcessResult {
        if (self.x == target.x && self.y == target.y) {
            //we are sigil under the usage stuff
            //first of all, we heal the stuff
            val healAmount = (healPerSigil * (1f + healPerSpirit / 100f * character.attributes.mind)).toInt()
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

class DivineConvergenceBehavior(config: SbMonsterAbilityConfiguration) : SkillBehavior() {

    private val lightDamagePerSigil = config.attributes.floatAttribute("lightDamagePerSigil")
    private val lightDamagePerSpirit = config.attributes.floatAttribute("lightDamagePerSpirit")
    private val healPerSigil = config.attributes.floatAttribute("healPerSigil")
    private val healPerSpirit = config.attributes.floatAttribute("healPerSpirit")
    private val healPerBody = config.attributes.floatAttribute("healPerBody")

    override fun ultimateUse(battle: Battle, character: Character, lucky: Boolean): ProcessResult {
        val tilesToExplode = character.field.tiles.filter { it.skin == TileSkin.VALERIAN_SIGIL_OF_RENEWAL_BG }
        val events = mutableListOf<BattleEvent>()
        val healAmount = (healPerSigil * tilesToExplode.size * (1f + character.attributes.spirit * healPerSpirit / 100f + character.attributes.body * healPerBody / 100f)).toInt()
        val damage = lightDamagePerSigil * tilesToExplode.size * (1f + character.attributes.spirit * lightDamagePerSpirit / 100f)

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
        val aoeResult = aoe.skillUse(battle, character, EMPTY_TILE, false)
        battle = aoeResult.battle
        events.addAll(aoeResult.events)

        return ProcessResult(listOf(BattleEvent.UltimateEvent(character.id, TileSkin.VALERIAN_DIVINE_CONVERGENCE, events)), battle)
    }
}
