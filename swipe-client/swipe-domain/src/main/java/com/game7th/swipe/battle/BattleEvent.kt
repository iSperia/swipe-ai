package com.game7th.swipe.battle

import com.game7th.swipe.battle.processor.TarotAnimation
import com.game7th.swipe.game.SbDisplayTileType

data class UnitPopup(
    val icons: List<String>,
    val text: String,
)

sealed interface BattleEvent {

    data class WaveEvent(
        val wave: Int
    ) : BattleEvent

    data class CreateUnitEvent(
        val id: Int,
        val skin: UnitSkin,
        val health: Int,
        val maxHealth: Int,
        val effects: List<Effect>,
        val team: Int,
        val scale: Float,
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
        val type: SbDisplayTileType,
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

    data class TileEffectEvent(
        val characterId: Int,
        val x: Int,
        val y: Int,
        val skin: TileSkin
    ) : BattleEvent

    data class BattleEndEvent(
        val team: Int
    ) : BattleEvent
}
