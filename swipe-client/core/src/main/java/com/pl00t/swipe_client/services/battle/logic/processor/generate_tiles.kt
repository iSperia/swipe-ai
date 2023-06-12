package com.pl00t.swipe_client.services.battle.logic.processor

import com.pl00t.swipe_client.services.battle.logic.TileSkin
import kotlin.random.Random

data class GenerationWeight(
    val tile: TileSkin,
    val weight: Int,
)

data class TileGeneratorConfig(
    val entries: List<GenerationWeight>
) {
    var totalWeight: Int? = null
    private fun total() = totalWeight ?: entries.sumOf { it.weight }.also { totalWeight = it }

    fun generate(): TileSkin {
        val random = Random.nextInt(total())
        var w = 0
        return entries.first {
            w += it.weight
            w > random
        }.tile
    }
}

object TileGeneratorConfigFactory {

    val MAX_STACKS = mapOf<TileSkin, Int>(
        TileSkin.VALERIAN_RADIANT_STRIKE to 3,
        TileSkin.VALERIAN_LUMINOUS_BEAM to 4,
        TileSkin.VALERIAN_SIGIL_OF_RENEWAL to 3,
        TileSkin.THORNSTALKER_PRIMAL_ASSAULT to 4,
        TileSkin.THORNSTALKER_VENOMOUS_BARRAGE to 4,
        TileSkin.THORNSTALKER_RESILIENT_GROWTH to 3,
        TileSkin.CORRUPTED_DRYAD_ARBOREAL_FANGS to 4,
        TileSkin.CORRUPTED_DRYAD_VILE_SIPHON to 4,
        TileSkin.CORRUPTED_DRYAD_SHADOWED_ANNIHILATION to 3,
    )
}
