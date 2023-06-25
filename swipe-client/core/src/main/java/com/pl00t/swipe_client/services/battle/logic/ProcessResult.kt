package com.pl00t.swipe_client.services.battle.logic

data class ProcessResult(
    val events: List<BattleEvent>,
    val battle: Battle
)
