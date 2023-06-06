package com.pl00t.swipe_client.services.battle.logic.processor

import com.pl00t.swipe_client.services.battle.logic.*
import com.pl00t.swipe_client.services.battle.logic.Unit

class SwipeProcesor {

    fun processSwipe(battle: Battle, unit: Int, dx: Int, dy: Int): ProcessResult {
        TODO("")
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
                    val x = position % 5
                    val y = position / 5
                    val skin = tileGeneratorConfig.generate()
                    Tile(
                        skin = skin,
                        progress = 1,
                        maxProgress = TileGeneratorConfigFactory.MAX_STACKS[skin] ?: 3,
                        x = x,
                        y = y,
                        id = tileId++
                    )
                }
                tiles.forEach { tile ->
                    events.add(BattleEvent.CreateTileEvent(unit.id, tile.x, tile.y, tile.skin, tile.progress, tile.maxProgress))
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
}
