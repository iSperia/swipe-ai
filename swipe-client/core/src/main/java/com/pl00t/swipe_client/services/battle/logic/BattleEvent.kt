package com.pl00t.swipe_client.services.battle.logic

import com.pl00t.swipe_client.services.battle.logic.processor.TarotAnimation

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
        val layer: Int,
        val type: TileType,
    ) : BattleEvent

    data class MoveTileEvent(
        val unitId: Int,
        val id: Int,
        val tox: Int,
        val toy: Int,
        val layer: Int,
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
        val stackLeft: Int,
        val layer: Int,
    ) : BattleEvent

    data class DestroyTileEvent(
        val unitId: Int,
        val id: Int,
        val layer: Int,
    ) : BattleEvent

    data class AnimateTarotEvent(
        val animation: TarotAnimation
    ) : BattleEvent

    data class UnitDeathEvent(
        val unitId: Int
    ) : BattleEvent

    data class UnitPopupEvent(
        val unitId: Int,
        val popup: UnitPopup,
    ) : BattleEvent

    data class UnitHealthEvent(
        val unitId: Int,
        val health: Int,
    ) : BattleEvent

    data class UltimateProgressEvent(
        val unitId: Int,
        val progress: Int,
        val maxProgress: Int
    ) : BattleEvent

    data class UltimateEvent(
        val unitId: Int,
        val skin: TileSkin,
        val events: List<BattleEvent>
    ) : BattleEvent
}
