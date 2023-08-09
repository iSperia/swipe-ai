package com.game7th.swipe.game

sealed interface SbEvent {
    data class EndOfSwipe(val characterId: Int): SbEvent
    data class EndOfTick(val characterId: Int): SbEvent
    data class UltimateUse(val characterId: Int): SbEvent
    data class TileReachedMaxProgress0(val characterId: Int, val tileId: Int): SbEvent
    data class TileReachedMaxProgress1(val characterId: Int, val tileId: Int): SbEvent
    data class DamageDealt(val characterId: Int, val sourceId: Int?, val damage: SbElemental): SbEvent
    data class CharacterPreDeath(val characterId: Int): SbEvent
}

typealias SbTrigger = (SbContext, SbEvent) -> Unit
