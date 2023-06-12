package com.pl00t.swipe_client.services.battle.logic

import com.pl00t.swipe_client.services.battle.MonsterConfiguration
import com.pl00t.swipe_client.services.battle.UnitSkin

data class CharacterAttributes(
    val mind: Int,
    val body: Int,
    val spirit: Int
)

data class HumanConfiguration(
    val configuration: MonsterConfiguration,
    val level: Int,
    val attributes: CharacterAttributes
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
    val characters: List<Character>,
    val swipeBeforeNpc: Int,
) {
    fun unitById(id: Int) = characters.firstOrNull { it.id == id }

    fun enemies(character: Character) = characters.filter { it.team != character.team }

    fun meleeTarget(character: Character) = enemies(character).shuffled().maxByOrNull { it.maxHealth }

    fun updateUnit(character: Character) = copy(characters = characters.map { if (it.id == character.id) character else it })

    fun updateOrRemoveUnit(character: Character) = copy(characters = characters.mapNotNull { when {
        character.id == it.id && character.health <= 0 -> null
        character.id == it.id && character.health > 0 -> character
        else -> it
    }})

    override fun toString(): String {
        return characters.joinToString("\n")
    }
}
