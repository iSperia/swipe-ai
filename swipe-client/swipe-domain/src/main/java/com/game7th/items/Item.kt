package com.game7th.items

enum class ItemAffixType {
    IMPLICIT_DARK_RESIST_FLAT,
    IMPLICIT_COLD_RESIST_FLAT,
    IMPLICIT_FIRE_RESIST_FLAT,
    IMPLICIT_SHOCK_RESIST_FLAT,
    IMPLICIT_PHYS_RESIST_FLAT,
    IMPLICIT_LIGHT_RESIST_FLAT,
    DARK_RESIST_FLAT,
    COLD_RESIST_FLAT,
    FIRE_RESIST_FLAT,
    SHOCK_RESIST_FLAT,
    PHYS_RESIST_FLAT,
    LIGHT_RESIST_FLAT
}

data class ItemAffix(
    val affix: ItemAffixType,
    val value: Float,
    val level: Int,
    val scalable: Boolean,
)

data class AffixGenerationConfig(
    val affix: ItemAffixType,
    val value: Float,
    val scalable: Boolean?,
)

data class ItemTemplate(
    val skin: String,
    val name: String,
    val lore: String,
    val implicit: List<ItemAffixType>,
    val category: ItemCategory
)

data class InventoryItem(
    val skin: String,
    val implicit: List<ItemAffix>,
    val affixes: List<ItemAffix>,
    val level: Int,
    val rarity: Int,
    val category: ItemCategory
)
