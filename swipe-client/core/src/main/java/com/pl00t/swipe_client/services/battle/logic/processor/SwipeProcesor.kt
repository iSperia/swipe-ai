package com.pl00t.swipe_client.services.battle.logic.processor

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.Unit
import kotlin.math.min
import kotlin.random.Random

class SwipeProcesor {

    private fun inb(x: Int, y: Int) = x > -1 && x < 5 && y > -1 && y < 5

    fun processSwipe(battle: Battle, unitId: Int, dx: Int, dy: Int): ProcessResult {
        println("process swipe $unitId $dx:$dy")
        val unit = battle.unitById(unitId) ?: return ProcessResult(emptyList(), battle)
        val fo = unit.field
        var fc = fo.copy(tiles = fo.tiles)
        val events = mutableListOf<BattleEvent>()
        val tilesToProcess = when {
            dx == -1 -> fo.tiles.sortedBy { it.x }
            dx == 1 -> fo.tiles.sortedByDescending { it.x }
            dy == -1 -> fo.tiles.sortedBy { it.y }
            dy == 1 -> fo.tiles.sortedByDescending { it.y }
            else -> fo.tiles.shuffled()
        }
        var tileId = unit.field.maxTileId
        tilesToProcess.forEach { ttp ->
            val tile = fc.tileBy(ttp.id) ?: ttp
            //TODO: check if tile is limited with distance
            val maxDistance = 5
            var tx = tile.x
            var ty = tile.y
            var merged = false
            println("step0 $tx:$ty")
            var dist = 0
            while (dist < maxDistance) {
                dist++
                val nx = tile.x + dist * dx
                val ny = tile.y + dist * dy
                //out of bounds, no move
                if (inb(nx, ny)) {
                    val t = fc.tileAt(nx, ny)
                    if (t != null) {
                        //Simple merge us into another stuff
                        if (t.skin == tile.skin && TileMerger.SIMPLE.contains(t.skin) && tile.progress < tile.maxProgress && t.progress < t.maxProgress) {
                            merged = true
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
                                stackLeft = stackLeft
                            )
                            println("====merge====\n$tile\n$t\n=============")
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
                events.add(BattleEvent.MoveTileEvent(unitId, tile.id, tx, ty))
                fc = fc.copy(tiles = fc.tiles.map { if (it.id == tile.id) tile.copy(x = tx, y = ty) else it })
            }
        }
        val freePosition = (0 until 25).shuffled().firstOrNull {
            fc.tileAt(it % 5, it / 5) == null
        }

        if (freePosition != null) {
            val tileConfig: TileGeneratorConfig = TileGeneratorConfigFactory.CONFIGS[unit.skin]!!
            val newTile = generateTile(freePosition, tileConfig, tileId++)
            fc = fc.copy(tiles = fc.tiles + newTile, maxTileId = tileId)
            events.add(BattleEvent.CreateTileEvent(
                unitId = unit.id,
                id = newTile.id,
                x = newTile.x,
                y = newTile.y,
                skin = newTile.skin,
                stack = newTile.progress,
                maxStack = newTile.maxProgress
            ))
        } else {
            //TODO: doom
        }

        var updatedBattle = battle.copy(units = battle.units.map { u ->
            if (u.id == unitId) {
                u.copy(field = fc)
            } else {
                u
            }
        })

        var needCheck = true
        while (needCheck) {
            needCheck = false
            fc.tiles.firstOrNull { it.progress >= it.maxProgress }?.let { tile ->
                val behavior = BehaviorFactory.behavior(tile.skin)
                if (behavior.autoDelete()) {
                    fc = fc.copy(tiles = fc.tiles.filterNot { it.id == tile.id })
                    events.add(BattleEvent.DestroyTileEvent(unitId, tile.id))
                }
                updatedBattle = battle.copy(units = battle.units.map { u ->
                    if (u.id == unitId) {
                        u.copy(field = fc)
                    } else {
                        u
                    }
                })
                val animation = behavior.animationStrategy(battle, unitId)
                events.add(BattleEvent.AnimateTarotEvent(animation))
                needCheck = true
            }
        }

        var swipes = updatedBattle.swipeBeforeNpc
        if (unit.human) {
            swipes = updatedBattle.swipeBeforeNpc - 1
            if (swipes <= 0) {
                swipes = updatedBattle.units.count { it.human }
                val bots = updatedBattle.units.filter { !it.human }.toList()
                bots.forEach { botUnit ->
                    updatedBattle.unitById(botUnit.id)?.let { bot ->
                        val directions = when (Random.nextInt(4)) {
                            0 -> -1 to 0
                            1 -> 1 to 0
                            2 -> 0 to 1
                            else -> 0 to -1
                        }
                        val result = processSwipe(updatedBattle, bot.id, directions.first, directions.second)
                        updatedBattle = result.battle
                        events.addAll(result.events)
                    }
                }
            }
        }

        return ProcessResult(
            events = events,
            battle = updatedBattle.copy(swipeBeforeNpc = swipes)
        )
    }

    fun processUltimate(battle: Battle, unit: Int): ProcessResult {
        TODO("")
    }

    fun createBattle(config: BattleConfiguration, battle: Battle): ProcessResult {
        var unitId = battle.maxUnitId
        val events = mutableListOf<BattleEvent>()
        val humanUnits = config.humans.map { humanConfig ->
            Unit(
                id = unitId++,
                field = TileField(emptyList(), 0),
                health = humanConfig.level * 50,
                maxHealth = humanConfig.level * 50,
                resists = ElementalConfig(),
                effects = emptyList(),
                skin = humanConfig.skin,
                level = humanConfig.level,
                human = true,
                team = 0
            )
        }
        val monsterUnits = config.waves.first().monsters.map { monsterConfig ->
            Unit(
                id = unitId++,
                field = TileField(emptyList(), 0),
                health = monsterConfig.level * 50,
                maxHealth = monsterConfig.level * 50,
                resists = ElementalConfig(),
                effects = emptyList(),
                skin = monsterConfig.skin,
                level = monsterConfig.level,
                human  = false,
                team = 1
            )
        }
        val units = humanUnits + monsterUnits
        val unitsWithTiles: List<Unit> = units.map { unit ->
            var tileId = unit.field.maxTileId
            val numTiles = 5
            val tileGeneratorConfig = TileGeneratorConfigFactory.CONFIGS[unit.skin]
            tileGeneratorConfig?.let { tileGenerationConfig ->
                val tiles = (0 until 25).shuffled().take(numTiles).map { position ->
                    generateTile(position, tileGeneratorConfig, tileId++)
                }
                tiles.forEach { tile ->
                    events.add(BattleEvent.CreateTileEvent(unit.id, tile.id, tile.x, tile.y, tile.skin, tile.progress, tile.maxProgress))
                }
                unit.copy(field = unit.field.copy(maxTileId = tileId, tiles = tiles))
            } ?: unit
        }
        events.addAll(units.map {
            BattleEvent.CreateUnitEvent(
                it.id,
                it.skin,
                it.health,
                it.maxHealth,
                it.effects,
                it.team
            )
        })
        val newBattle = battle.copy(maxUnitId = unitId, units = unitsWithTiles)
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
            id = tileId
        )
    }
}
