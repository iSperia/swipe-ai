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
        val team: Int,
    ) : BattleEvent

    data class CreateTileEvent(
        val unitId: Int,
        val id: Int,
        val x: Int,
        val y: Int,
        val skin: TileSkin,
        val stack: Int,
        val maxStack: Int,
    ) : BattleEvent

    data class MoveTileEvent(
        val unitId: Int,
        val id: Int,
        val tox: Int,
        val toy: Int,
    ) : BattleEvent

    data class MergeTileEvent(
        val unitId: Int,
        val id: Int,
        val to: Int,
        val tox: Int,
        val toy: Int,
        val ttox: Int,
        val ttoy: Int,
        val targetStack: Int,
        val stackLeft: Int
    ) : BattleEvent

    data class DestroyTileEvent(
        val unitId: Int,
        val id: Int
    ) : BattleEvent
}
