package com.game7th.swipe.battle

import com.google.gson.JsonObject
import com.game7th.swipe.battle.processor.TileGeneratorConfig

enum class UnitSkin {
    CHARACTER_VALERIAN,
    CHARACTER_SAFFRON,
    MONSTER_THORNSTALKER,
    MONSTER_CORRUPTED_DRYAD,
    MONSTER_THALENDROS,
    MONSTER_THORNED_CRAWLER
}

fun JsonObject.intAttribute(key: String) = this.get(key).asInt
fun JsonObject.floatAttribute(key: String) = this.get(key).asFloat
fun JsonObject.stringAttribute(key: String) = this.get(key)?.asString ?: ""

data class MonsterAbilityDescriptionRow(
    val title: String,
    val description: String,
) {
    fun formatDescription(monsterInfo: SbMonsterConfiguration): String {
        val regex = """\{([^{}]*)\}""".toRegex()
        val groups = regex.findAll(description).map { it.groupValues[1] }.toList()
        var formatted = description
        groups.forEach {  group ->
            formatted = formatted.replace("{$group}", monsterInfo.balance.stringAttribute(group))
        }
        return formatted
    }
}

data class SbMonsterAbilityConfiguration(
    val title: String,
    val skin: TileSkin,
    val descriptionTable: List<MonsterAbilityDescriptionRow>,
    val attributes: JsonObject,
    val description: String,
    val lore: String,
)

data class SbMonsterConfiguration(
    val skin: String,
    val name: String,
    val lore: String,
    val balance: JsonObject,
    val triggers: List<String>,
    val tiles: List<TileGeneratorConfig>,
    val attributes: CharacterAttributes,
    val tileConfig: TileGeneratorConfig,
    val scale: Float,
    val level: Int,
    val abilities: List<SbMonsterAbilityConfiguration>?,
)
