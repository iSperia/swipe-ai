package com.pl00t.swipe_client.services.battle.logic

data class UnitPopup(
    val icons: List<String>,
    val text: String,
)

sealed interface BattleEvent {

    data class CreateUnitEvent(
        val id: Int,
        val skin: UnitSkin,
        val health: Int,
        val maxHealth: Int,
        val effects: List<Effect>,
    )

    data class CreateTileEvent(
        val id: Int,
        val x: Int,
        val y: Int,
        val skin: TileSkin,
        val stack: Int,
        val maxStack: Int,
    )
}
