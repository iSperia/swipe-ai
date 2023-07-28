package com.game7th.swipe.game

data class CharacterAttributes(
    val mind: Int,
    val body: Int,
    val spirit: Int
) {
    companion object {
        val ZERO = CharacterAttributes(0, 0, 0)
    }
}

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
