package com.pl00t.swipe_client.services.battle.logic.processor

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.Character
import com.pl00t.swipe_client.services.battle.logic.processor.skills.characters.DivineConvergenceBehavior
import kotlin.math.min
import kotlin.random.Random

class SwipeProcesor {

    private fun inb(x: Int, y: Int) = x > -1 && x < 5 && y > -1 && y < 5

    fun processSwipe(battle: Battle, unitId: Int, dx: Int, dy: Int): ProcessResult {
        var character = battle.unitById(unitId) ?: return ProcessResult(emptyList(), battle)
        val fo = character.field
        var fc = fo.copy(tiles = fo.tiles)
        val events = mutableListOf<BattleEvent>()
        val tilesToProcess = when {
            dx == -1 -> fo.tiles.sortedBy { it.x }
            dx == 1 -> fo.tiles.sortedByDescending { it.x }
            dy == -1 -> fo.tiles.sortedBy { it.y }
            dy == 1 -> fo.tiles.sortedByDescending { it.y }
            else -> fo.tiles.shuffled()
        }
        var mergedCount = 0
        tilesToProcess.forEach { ttp ->
            val tile = fc.tileBy(ttp.id) ?: ttp
            val maxDistance = min(5, tile.mobility)
            var tx = tile.x
            var ty = tile.y
            var merged = false
            var dist = 0
            while (dist < maxDistance) {
                dist++
                val nx = tile.x + dist * dx
                val ny = tile.y + dist * dy
                //out of bounds, no move
                if (inb(nx, ny)) {
                    val t = fc.tileAt(nx, ny, tile.layer)
                    if (t != null) {
                        //Simple merge us into another stuff
                        if (t.skin == tile.skin && TileMerger.SIMPLE.contains(t.skin) && tile.progress < tile.maxProgress && t.progress < t.maxProgress) {
                            merged = true
                            mergedCount++
                            val sumStack = t.progress + tile.progress
                            val stackLeft = 0
                            val event = BattleEvent.MergeTileEvent(
                                unitId = unitId,
                                id = tile.id,
                                to = t.id,
                                tox = tx,
                                toy = ty,
                                ttox = t.x,
                                ttoy = t.y,
                                targetStack = min(t.maxProgress, sumStack),
                                stackLeft = stackLeft,
                                layer = t.layer
                            )
                            events.add(event)
                            fc = fc.copy(tiles = fc.tiles.mapNotNull { fct ->
                                if (fct.id == t.id) {
                                    fct.copy(progress = min(sumStack, fct.maxProgress))
                                } else if (fct.id == tile.id) {
                                    if (stackLeft == 0) null else fct.copy(progress = stackLeft, x = tx, y = ty)
                                } else {
                                    fct
                                }
                            })
                        }
                        break
                    } else {
                        tx = nx
                        ty = ny
                    }
                } else {
                    break
                }
            }
            if (!merged && tx != tile.x || ty != tile.y) {
                events.add(BattleEvent.MoveTileEvent(unitId, tile.id, tx, ty, tile.layer))
                fc = fc.copy(tiles = fc.tiles.map { if (it.id == tile.id) tile.copy(x = tx, y = ty) else it })
            }
        }
        character = character.updateField(fc)
        if (mergedCount > 0) {
            val comboAmount = (mergedCount + 10f * (1f + 0.05f * character.attributes.mind + 0.5f * character.combo)).toInt()
            val newUltimateProgress = min(character.maxUltimateProgress, character.ultimateProgress + comboAmount)
            character = character.updateUltimateProgress(character.combo + 1, newUltimateProgress)
            events.add(BattleEvent.UltimateProgressEvent(character.id, character.ultimateProgress, character.maxUltimateProgress))
        } else {
            character = character.updateUltimateProgress(0, character.ultimateProgress)
        }

        val freePosition = (0 until 25).shuffled().firstOrNull {
            fc.tileAt(it % 5, it / 5, 5) == null
        }

        if (freePosition != null) {
            val tileConfig: TileGeneratorConfig = character.tileConfig
            val newTile = generateTile(freePosition, tileConfig, character.field.maxTileId)
            character = character.addTile(newTile)
            events.add(BattleEvent.CreateTileEvent(
                unitId = character.id,
                id = newTile.id,
                x = newTile.x,
                y = newTile.y,
                skin = newTile.skin,
                stack = newTile.progress,
                maxStack = newTile.maxProgress,
                layer = newTile.layer,
                type = newTile.type,
            ))
        } else {
            //TODO: doom
        }

        var battle = battle.updateOrRemoveUnit(character)

        var needCheck = true
        while (needCheck) {
            needCheck = false
            character.field.tiles.firstOrNull { it.maxProgress > 1 && it.progress >= it.maxProgress }?.let { tile ->
                val behavior = BehaviorFactory.behavior(tile.skin)
                if (behavior.autoDelete()) {
                    character = character.removeTile(tile.id)
                    battle = battle.updateOrRemoveUnit(character)
                    events.add(BattleEvent.DestroyTileEvent(unitId, tile.id, tile.layer))
                }
                val animation = behavior.animationStrategy(battle, unitId)
                events.add(BattleEvent.AnimateTarotEvent(animation))

                val useResult = behavior.skillUse(battle, character, false)
                battle = useResult.battle
                character = battle.unitById(character.id) ?: character
                events.addAll(useResult.events)

                fc.tiles.forEach { triggerTile ->
                    val triggerBehavior = BehaviorFactory.behavior(triggerTile.skin)
                    val triggerResult = triggerBehavior.afterTileUsed(battle, character, triggerTile, tile)
                    battle = triggerResult.battle
                    character = battle.unitById(character.id) ?: character
                    events.addAll(triggerResult.events)
                }

                needCheck = true
            }
        }

        var swipes = battle.swipeBeforeNpc
        if (character.human) {
            swipes = battle.swipeBeforeNpc - 1
            if (swipes <= 0) {
                swipes = battle.characters.count { it.human }
                val bots = battle.characters.filter { !it.human }.toList()
                bots.forEach { botUnit ->
                    battle.unitById(botUnit.id)?.let { bot ->
                        val directions = when (Random.nextInt(4)) {
                            0 -> -1 to 0
                            1 -> 1 to 0
                            2 -> 0 to 1
                            else -> 0 to -1
                        }
                        val result = processSwipe(battle, bot.id, directions.first, directions.second)
                        battle = result.battle
                        events.addAll(result.events)
                    }
                }
            }
        }

        battle.unitById(character.id)?.let { character ->
            character.field.tiles.forEach { eotTile ->
                val behavior = BehaviorFactory.behavior(eotTile.skin)
                val eotResult = behavior.onEndOfTurn(battle, character.id, eotTile)
                battle = eotResult.battle

                events.addAll(eotResult.events)
            }
        }

        return ProcessResult(
            events = events,
            battle = battle.copy(swipeBeforeNpc = swipes)
        )
    }

    fun processUltimate(battle: Battle, unit: Int): ProcessResult {
        battle.unitById(unit)?.let { character ->
            val behavior = BehaviorFactory.behavior(character.ultimateBehavior)
            val result = behavior.skillUse(battle, character, false)
            result.battle.unitById(unit)?.let { character ->
                val character = character.copy(ultimateProgress = 0, combo = 0)
                val battle = result.battle.updateOrRemoveUnit(character)
                val event = BattleEvent.UltimateProgressEvent(character.id, character.ultimateProgress, character.maxUltimateProgress)
                return result.copy(battle = battle, events = result.events + event)
            } ?: return result
        } ?: return ProcessResult(emptyList(), battle)
    }

    fun createBattle(config: BattleConfiguration, battle: Battle, wave: Int): ProcessResult {
        var unitId = battle.maxUnitId
        val events = mutableListOf<BattleEvent>()
        val humanCharacters = if (wave < 1) config.humans.map { humanConfig ->
            val health = (100 * (1f + humanConfig.attributes.body * 0.1f)).toInt()
            Character(
                id = unitId++,
                field = TileField(emptyList(), 0),
                health = health,
                maxHealth = health,
                resists = ElementalConfig(),
                effects = emptyList(),
                skin = humanConfig.configuration.skin,
                level = humanConfig.level,
                human = true,
                team = 0,
                attributes = humanConfig.attributes,
                ultimateProgress = 0,
                maxUltimateProgress = 1000,
                combo = 0,
                ultimateBehavior = TileSkin.VALERIAN_DIVINE_CONVERGENCE,
                scale = 1f,
                tileConfig = humanConfig.configuration.tileConfig
            )
        } else {
            emptyList()
        }

        val monstersWithAttributes = config.waves[wave].monsters.map { monsterConfig ->
            val amountOfAttributes = (Math.pow(1.05, monsterConfig.level.toDouble()).toFloat() * monsterConfig.level * 3).toInt()
            var body = 0
            var spirit = 0
            var mind = 0
            (1..amountOfAttributes).forEach {
                val r = Random.nextFloat()
                when {
                    r <= 0.3333f -> body++
                    r <= 0.6666f -> spirit++
                    else -> mind++
                }
            }
            monsterConfig to CharacterAttributes(mind, body, spirit)
        }

        val monsterCharacters = monstersWithAttributes.map { (monsterConfig, attributes) ->
            val health = (monsterConfig.baseHealth * (1f + attributes.body * 0.1f)).toInt()
            Character(
                id = unitId++,
                field = TileField(emptyList(), 0),
                health = health,
                maxHealth = health,
                resists = ElementalConfig(),
                effects = emptyList(),
                skin = monsterConfig.skin,
                level = monsterConfig.level,
                human  = false,
                team = 1,
                attributes = attributes,
                ultimateProgress = 0,
                maxUltimateProgress = 1000,
                combo = 0,
                ultimateBehavior = TileSkin.VALERIAN_SIGIL_OF_RENEWAL_BG,
                scale = monsterConfig.scale,
                tileConfig = monsterConfig.tileConfig,
            )
        }
        val units = humanCharacters + monsterCharacters
        val unitsWithTiles: List<Character> = units.map { unit ->
            var tileId = unit.field.maxTileId
            val numTiles = 5
            val tileGeneratorConfig = unit.tileConfig
            val tiles = (0 until 25).shuffled().take(numTiles).map { position ->
                generateTile(position, tileGeneratorConfig, tileId++)
            }
            tiles.forEach { tile ->
                events.add(BattleEvent.CreateTileEvent(unit.id, tile.id, tile.x, tile.y, tile.skin, tile.progress, tile.maxProgress, tile.layer, tile.type))
            }
            unit.copy(field = unit.field.copy(maxTileId = tileId, tiles = tiles))
        }
        events.addAll(units.map {
            BattleEvent.CreateUnitEvent(
                it.id,
                it.skin,
                it.health,
                it.maxHealth,
                it.effects,
                it.team,
                it.scale,
            )
        })
        val newBattle = battle.copy(maxUnitId = unitId, characters = battle.characters + unitsWithTiles)
        return ProcessResult(battle = newBattle, events = events)
    }

    private fun generateTile(
        position: Int,
        tileGeneratorConfig: TileGeneratorConfig,
        tileId: Int
    ): Tile {
        val x = position % 5
        val y = position / 5
        val skin = tileGeneratorConfig.generate()
        return Tile(
            skin = skin,
            progress = 1,
            maxProgress = TileGeneratorConfigFactory.MAX_STACKS[skin] ?: 3,
            x = x,
            y = y,
            id = tileId,
            layer = 5,
            mobility = 5,
            type = TileType.TAROT,
        )
    }
}
