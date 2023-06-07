package com.pl00t.swipe_client.services.battle.logic.processor

import com.pl00t.swipe_client.services.battle.logic.Battle
import com.pl00t.swipe_client.services.battle.logic.ProcessResult
import com.pl00t.swipe_client.services.battle.logic.TileSkin

data class ExecuteStrategy(
    val delete: Boolean,
    val action: (Battle, Int, Int) -> ProcessResult
)

object Executors {

    val EXECUTORS = mapOf<TileSkin, ExecuteStrategy>(
        TileSkin.VALERIAN_RADIANT_STRIKE to ExecuteStrategy(true) { b, u, t -> ProcessResult(emptyList(), b) },
        TileSkin.VALERIAN_LUMINOUS_BEAM to ExecuteStrategy(true) { b, u, t -> ProcessResult(emptyList(), b) },
        TileSkin.VALERIAN_SIGIL_OF_RENEWAL to ExecuteStrategy(true) { b, u, t -> ProcessResult(emptyList(), b) },
    )
}
