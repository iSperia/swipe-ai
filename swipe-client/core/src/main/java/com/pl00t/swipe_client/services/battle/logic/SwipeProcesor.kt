package com.pl00t.swipe_client.services.battle.logic

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
                field = TileField(emptyList()),
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
                field = TileField(emptyList()),
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
        events.addAll(units.map { BattleEvent.CreateUnitEvent(it.id, it.skin, it.health, it.maxHealth, it.effects, it.team) })
        val newBattle = battle.copy(maxUnitId = unitId, units = units)
        return ProcessResult(battle = newBattle, events = events)
    }
}
