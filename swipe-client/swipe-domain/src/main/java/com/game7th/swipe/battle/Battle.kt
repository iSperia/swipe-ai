package com.game7th.swipe.battle

data class CharacterAttributes(
    val mind: Int,
    val body: Int,
    val spirit: Int
) {
    companion object {
        val ZERO = CharacterAttributes(0, 0, 0)
    }
}

data class HumanConfiguration(
    val configuration: SbMonsterEntry,
    val level: Int,
    val attributes: CharacterAttributes
)

data class MonsterWaveConfiguration(
    val monsters: List<SbMonsterEntry>
)

data class BattleConfiguration(
    val humans: List<HumanConfiguration>,
    val waves: List<MonsterWaveConfiguration>
)

data class SbHumanEntry(
    val skin: String,
    val level: Int,
    val attributes: CharacterAttributes
)

data class SbMonsterEntry(
    val skin: String,
    val level: Int,
)

data class SbMonsterWaveConfiguration(
    val monsters: List<SbMonsterEntry>
)

data class Battle(
    val maxUnitId: Int,
    val characters: List<Character>,
    val swipeBeforeNpc: Int,
    val waves: List<MonsterWaveConfiguration>,
    val activeWave: Int,
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
