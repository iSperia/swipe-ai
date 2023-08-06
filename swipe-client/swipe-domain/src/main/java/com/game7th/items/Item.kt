package com.game7th.items

import com.game7th.swipe.SbText

enum class ItemAffixType(val pattern: String) {
    DARK_RESIST_FLAT("%.0f"),
    COLD_RESIST_FLAT("%.0f"),
    FIRE_RESIST_FLAT("%.0f"),
    SHOCK_RESIST_FLAT("%.0f"),
    PHYS_RESIST_FLAT("%.0f"),
    LIGHT_RESIST_FLAT("%.0f"),
    DARK_DAMAGE_INCREASE("%.0f"),
    COLD_DAMAGE_INCREASE("%.0f"),
    FIRE_DAMAGE_INCREASE("%.0f"),
    SHOCK_DAMAGE_INCREASE("%.0f"),
    LIGHT_DAMAGE_INCREASE("%.0f"),
    PHYS_DAMAGE_INCREASE("%.0f"),
    FLAT_BODY("%.0f"),
    FLAT_SPIRIT("%.0f"),
    FLAT_MIND("%.0f"),
    FLAT_HP("%.0f"),
    PERCENT_HP("%.0f"),
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
    val name: SbText,
    val implicit: ItemAffixType,
    val category: ItemCategory
)

data class InventoryItem(
    val id: String,
    val skin: String,
    val implicit: ItemAffix,
    val affixes: List<ItemAffix>,
    val experience: Int,
    val rarity: Int,
    val category: ItemCategory,
    val equippedBy: String?,
    val maxExperience: Int,
)

