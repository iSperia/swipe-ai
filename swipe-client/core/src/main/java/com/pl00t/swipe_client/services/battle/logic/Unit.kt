package com.pl00t.swipe_client.services.battle.logic

data class Unit(
    val id: Int,
    val field: TileField,
    val health: Int,
    val maxHealth: Int,
    val resists: ElementalConfig,
    val effects: List<Effect>,
    val skin: UnitSkin,
    val level: Int,
    val human: Boolean,
    val team: Int
)

enum class UnitSkin {
    CHARACTER_VALERIAN,
    MONSTER_THORNSTALKER,
    MONSTER_CORRUPTED_DRYAD,
    MONSTER_THALENDROS
}
