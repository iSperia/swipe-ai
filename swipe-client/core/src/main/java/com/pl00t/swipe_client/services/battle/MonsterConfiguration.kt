package com.pl00t.swipe_client.services.battle

import com.google.gson.JsonObject
import com.pl00t.swipe_client.services.battle.logic.TileSkin
import com.pl00t.swipe_client.services.battle.logic.processor.TileGeneratorConfig

enum class UnitSkin {
    CHARACTER_VALERIAN,
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
    fun formatDescription(attributes: JsonObject): String {
        val regex = """\{([^{}]*)\}""".toRegex()
        val groups = regex.findAll(description).map { it.groupValues[1] }.toList()
        var formatted = description
        groups.forEach {  group ->
            formatted = formatted.replace("{$group}", attributes.stringAttribute(group))
        }
        return formatted
    }
}

data class MonsterAbilityConfiguration(
    val title: String,
    val skin: TileSkin,
    val attributes: JsonObject,
    val descriptionTable: List<MonsterAbilityDescriptionRow>,
    val description: String,
    val lore: String,
)

data class MonsterConfiguration(
    val skin: UnitSkin,
    val name: String,
    val baseHealth: Int,
    val tileConfig: TileGeneratorConfig,
    val scale: Float,
    val lore: String,
    val level: Int,
    val abilities: List<MonsterAbilityConfiguration>?,
) {
    companion object {
        val DEFAULT = MonsterConfiguration(
            skin = UnitSkin.CHARACTER_VALERIAN,
            name = "Cabbage Head",
            baseHealth = 10,
            tileConfig = TileGeneratorConfig(emptyList()),
            abilities = emptyList(),
            scale = 1f,
            lore = "No Lore for Cabbage",
            level = 1
        )
    }
}
