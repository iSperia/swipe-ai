package com.game7th.items

enum class ItemAffixType {
    DARK_RESIST_FLAT,
    COLD_RESIST_FLAT,
    FIRE_RESIST_FLAT,
    SHOCK_RESIST_FLAT,
    PHYS_RESIST_FLAT,
    LIGHT_RESIST_FLAT,
    DARK_DAMAGE_INCREASE,
    COLD_DAMAGE_INCREASE,
    FIRE_DAMAGE_INCREASE,
    SHOCK_DAMAGE_INCREASE,
    LIGHT_DAMAGE_INCREASE,
    PHYS_DAMAGE_INCREASE,
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
    val id: String,
    val skin: String,
    val implicit: List<ItemAffix>,
    val affixes: List<ItemAffix>,
    val level: Int,
    val maxLevel: Int,
    val rarity: Int,
    val category: ItemCategory,
    val equippedBy: String?,
    val experience: Int,
)

