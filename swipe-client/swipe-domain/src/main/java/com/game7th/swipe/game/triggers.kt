package com.game7th.swipe.game

sealed interface SbEvent {
    data class EndOfSwipe(val characterId: Int): SbEvent
    data class EndOfTick(val characterId: Int): SbEvent
    data class UltimateUse(val characterId: Int): SbEvent
}

typealias SbTrigger = (SbEvent) -> Unit
