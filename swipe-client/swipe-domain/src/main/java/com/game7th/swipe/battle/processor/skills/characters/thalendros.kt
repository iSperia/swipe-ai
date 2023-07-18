package com.game7th.swipe.battle.processor.skills.characters

import com.game7th.swipe.battle.*
import com.game7th.swipe.battle.processor.SkillBehavior
import com.game7th.swipe.battle.processor.TarotAnimation
import com.game7th.swipe.battle.processor.animateMeleeAttack
import com.game7th.swipe.battle.processor.skills.MeleeAttackSkillBehavior
import com.game7th.swipe.battle.processor.skills.dealDamage

class ThornedWhipBehavior(
    config: SbMonsterAbilityConfiguration
) : SkillBehavior() {

    private val physDamage = config.attributes.floatAttribute("physDamage")
    private val physPerBody = config.attributes.floatAttribute("physPerBody")

    private val melee = MeleeAttackSkillBehavior { battle, character ->
        val physicalDamage = physDamage * (1f + character.attributes.body * physPerBody / 100f)
        ElementalConfig(physical = physicalDamage)
    }

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THALENDROS_THORN_WHIP)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean) = melee.skillUse(battle, character, at, lucky)
}

class EarthquakeSlam(
    config: SbMonsterAbilityConfiguration
) : SkillBehavior() {

    private val tilesCount = config.attributes.intAttribute("numTiles")
    private val resistBuff = config.attributes.floatAttribute("resistBuff")

    override fun animationStrategy(battle: Battle, unitId: Int) = animateMeleeAttack(battle, unitId, TileSkin.THALENDROS_EARTHQUAKE_SLAM)

    override fun skillUse(battle: Battle, character: Character, at: Tile, lucky: Boolean): ProcessResult {
        val positions = (0 until 25).shuffled().take(if (lucky) tilesCount * 2 else tilesCount)
        var enemy = battle.enemies(character).random()
        val events = mutableListOf<BattleEvent>()
        var battle = battle
        positions.forEach { p ->
            events.add(BattleEvent.TileEffectEvent(enemy.id, p % 5, p / 5, TileSkin.THALENDROS_CORRUPTED_ROOTS))
            enemy.field.tiles.filter { it.x == p % 5 && it.y == p / 5 }.forEach { tileToRemove ->
                events.add(BattleEvent.DestroyTileEvent(enemy.id, tileToRemove.id, tileToRemove.layer))
            }
            enemy = enemy.copy(field = enemy.field.copy(
                tiles = enemy.field.tiles.filter { it.x != p % 5 || it.y != p / 5 }
            ))
            battle = battle.updateOrRemoveUnit(enemy)

            val tile = Tile(
                skin = TileSkin.THALENDROS_CORRUPTED_ROOTS,
                progress = 1,
                maxProgress = tilesCount,
                x = p % 5,
                y = p / 5,
                id = enemy.field.maxTileId,
                layer = 5,
                mobility = 5,
                type = TileType.TAROT,
                meta = null
            )
            enemy = enemy.addTile(tile)
            events.add(BattleEvent.CreateTileEvent(enemy.id, tile.id, tile.x, tile.y, tile.skin, tile.progress, tile.maxProgress, tile.layer, tile.type))
            battle = battle.updateOrRemoveUnit(enemy)
        }
        return ProcessResult(events, battle)
    }
}

class CorruptedRoots(
    config: SbMonsterAbilityConfiguration
) : SkillBehavior() {
}

class DarkAura(
    config: SbMonsterAbilityConfiguration
) : SkillBehavior() {

    private val numTiles = config.attributes.intAttribute("numTiles")
    private val darkDamage = config.attributes.floatAttribute("darkDamage")
    private val darkPerSpirit = config.attributes.floatAttribute("darkPerSpirit")

    override fun afterTileUsed(battle: Battle, character: Character, self: Tile, target: Tile): ProcessResult {
        val events = mutableListOf<BattleEvent>()
        var battle = battle
        var enemy = battle.enemies(character).randomOrNull() ?: return ProcessResult(events, battle)
        events.add(BattleEvent.AnimateTarotEvent(TarotAnimation.TarotAtSourceRotate(TileSkin.THALENDROS_DARK_AURA, enemy.id)))
        val filledPositions = enemy.field.tiles.filter { it.layer == 0 }.map { it.x + it.y * 5 }.toSet()
        val positions = (0 until 25).filter { !filledPositions.contains(it) }.shuffled().take(numTiles)
        var tileId = enemy.field.maxTileId
        var field = enemy.field
        positions.forEach { position ->
            val x = position % 5
            val y = position / 5
            val tile = Tile(
                skin = TileSkin.THALENDROS_DARK_TILE,
                progress = 0,
                maxProgress = 0,
                x = x,
                y = y,
                id = tileId++,
                layer = 0,
                mobility = 0,
                meta = darkDamage + darkPerSpirit * character.attributes.spirit,
                type = TileType.BACKGROUND
            )
            field = field.copy(tiles = field.tiles + tile, maxTileId = tileId)
            events.add(
                BattleEvent.CreateTileEvent(
                unitId = enemy.id,
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
        enemy = enemy.copy(field = field)
        battle = battle.updateOrRemoveUnit(enemy)
        return ProcessResult(events, battle)
    }
}

class DarkTile(config: SbMonsterAbilityConfiguration) : SkillBehavior() {

    override fun afterTileUsed(battle: Battle, character: Character, self: Tile, target: Tile): ProcessResult {
        if (self.x != target.x || self.y != target.y) return ProcessResult(emptyList(), battle)
        val amount = self.meta as? Float ?: 0f
        val damage = ElementalConfig(
                physical = 0f,
                cold = 0f,
                fire = 0f,
                light = 0f,
                shock = 0f,
                dark = amount
            )

        val character = character.removeTile(self.id)
        var battle = battle.updateOrRemoveUnit(character)
        val events = mutableListOf<BattleEvent>()
        events.add(BattleEvent.DestroyTileEvent(character.id, self.id, self.layer))

        val damageResult = battle.dealDamage(character, character, self, damage)
        return ProcessResult(damageResult.events + events, damageResult.battle)
    }
}
