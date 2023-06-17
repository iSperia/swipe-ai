package com.pl00t.swipe_client.services.battle

import com.pl00t.swipe_client.services.battle.logic.processor.TileGeneratorConfig

enum class UnitSkin {
    CHARACTER_VALERIAN,
    MONSTER_THORNSTALKER,
    MONSTER_CORRUPTED_DRYAD,
    MONSTER_THALENDROS,
    MONSTER_THORNED_CRAWLER
}

data class MonsterConfiguration(
    val skin: UnitSkin,
    val name: String,
    val baseHealth: Int,
    val tileConfig: TileGeneratorConfig,
    val scale: Float,
    val f1: Float,
    val f2: Float,
    val f3: Float,
    val f4: Float,
    val f5: Float,
    val i1: Int,
    val i2: Int,
    val i3: Int,
    val level: Int,
)
