package com.pl00t.swipe_client.services.battle.logic

data class HumanConfiguration(
    val skin: UnitSkin,
    val level: Int,
    val mind: Int,
    val body: Int,
    val spirit: Int,
)

data class MonsterConfiguration(
    val skin: UnitSkin,
    val level: Int,
)

data class MonsterWaveConfiguration(
    val monsters: List<MonsterConfiguration>
)

data class BattleConfiguration(
    val humans: List<HumanConfiguration>,
    val waves: List<MonsterWaveConfiguration>
)

data class Battle(
    val maxUnitId: Int,
    val units: List<Unit>,
    val swipeBeforeNpc: Int,
) {
    fun unitById(id: Int) = units.firstOrNull { it.id == id }

    fun enemies(unit: Unit) = units.filter { it.team != unit.team }

    fun meleeTarget(unit: Unit) = enemies(unit).lastOrNull()
}
