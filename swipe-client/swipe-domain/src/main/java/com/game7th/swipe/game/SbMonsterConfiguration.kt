package com.game7th.swipe.game

import com.game7th.swipe.SbText
import com.google.gson.JsonObject

fun JsonObject.intAttribute(key: String) = this.get(key).asInt
fun JsonObject.floatAttribute(key: String) = this.get(key).asFloat
fun JsonObject.stringAttribute(key: String) = this.get(key)?.asString ?: ""

data class SbMonsterAbilityConfiguration(
    val id: String,
    val weight: Int,
    val timeout: Int,
)

data class FrontMonsterConfiguration(
    val skin: String,
    val name: SbText,
    val level: Int,
    val rarity: Int,
    val attributes: CharacterAttributes,
    val resist: SbElemental,
    val damage: SbElemental,
    val frontAbilities: List<FrontMonsterAbility>,
    val abilities: List<SbMonsterAbilityConfiguration>,
    val health: Int,
    val luck: Float,
    val ult: Int,
    val ultMax: Int,
    val lore: SbText,
    val ultPrefillPercent: Int,
    val rarityAffixes: List<SbMonsterRarityAffix>
)

enum class SbMonsterRarityAffix {
    EXTRA_HP, EXTRA_LUCK, EXTRA_ULT_PROGRESS,
    ALL_RESIST, ALL_ATTRIBUTES, ALL_DAMAGE,
    EXTRA_BODY, EXTRA_MIND, EXTRA_SPIRIT
}

data class SbMonsterConfiguration(
    val skin: String,
    val name: SbText,
    val rarity: Int,
    val balance: JsonObject,
    val triggers: List<String>,
    val tiles: List<SbTileTemplate>?,
    val attributes: CharacterAttributes,
    val scale: Float,
    val cap: Float,
    val level: Int,
    val lore: SbText,
    val abilities: List<SbMonsterAbilityConfiguration>?
)
