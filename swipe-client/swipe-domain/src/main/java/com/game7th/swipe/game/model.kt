package com.game7th.swipe.game

import com.game7th.items.InventoryItem

data class CharacterAttributes(
    val mind: Int,
    val body: Int,
    val spirit: Int
) {
    companion object {
        val ZERO = CharacterAttributes(0, 0, 0)
    }
}


data class SbMonsterEntry(
    val skin: String,
    val level: Int,
    val rarity: Int,
)

data class SbMonsterWaveConfiguration(
    val monsters: List<SbMonsterEntry>
)
