package com.game7th.items

enum class ItemAffix {
    DARK_RESIST_FLAT,
    COLD_RESIST_FLAT,
    FIRE_RESIST_FLAT,
    SHOCK_RESIST_FLAT,
    PHYS_RESIST_FLAT,
    LIGHT_RESIST_FLAT
}

data class AffixEntry(
    val affix: ItemAffix,
    val value: Int,
    val level: Int,
    val scalable: Boolean,
)

data class AffixGenerationConfig(
    val affix: ItemAffix,
    val min: Int,
    val max: Int,
    val scalable: Boolean?,
)

data class InventoryItem(
    val skin: String,
    val implicit: List<AffixEntry>,
    val affixes: List<AffixEntry>,
    val level: Int,
    val rarity: Int,
)
