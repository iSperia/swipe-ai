package com.pl00t.swipe_client.services.battle.logic.processor

import com.pl00t.swipe_client.services.battle.logic.TileSkin
import com.pl00t.swipe_client.services.battle.logic.UnitSkin
import kotlin.random.Random

data class GenerationWeight(
    val tile: TileSkin,
    val weight: Int,
)

data class TileGenerationConfig(
    val entries: List<GenerationWeight>
) {
    val totalWeight = entries.sumOf { it.weight }

    fun generate(): TileSkin {
        val random = Random.nextInt(totalWeight)
        var w = 0
        return entries.first {
            w += it.weight
            w > random
        }.tile
    }
}

object TileGeneratorConfigFactory {
    val CONFIGS = mapOf<UnitSkin, TileGenerationConfig>(
        UnitSkin.CHARACTER_VALERIAN to TileGenerationConfig(
            entries = listOf(
                GenerationWeight(TileSkin.VALERIAN_RADIANT_STRIKE, 3),
                GenerationWeight(TileSkin.VALERIAN_LUMINOUS_BEAM, 3),
                GenerationWeight(TileSkin.VALERIAN_SIGIL_OF_RENEWAL, 3))
        ),
        UnitSkin.MONSTER_THORNSTALKER to TileGenerationConfig(
            entries = listOf(
                GenerationWeight(TileSkin.THORNSTALKER_PRIMAL_ASSAULT, 5),
                GenerationWeight(TileSkin.THORNSTALKER_RESILIENT_GROWTH, 3),
                GenerationWeight(TileSkin.THORNSTALKER_VENOMOUS_BARRAGE, 2),
            )
        ),
        UnitSkin.MONSTER_CORRUPTED_DRYAD to TileGenerationConfig(
            entries = listOf(
                GenerationWeight(TileSkin.CORRUPTED_DRYAD_ARBOREAL_FANGS, 4),
                GenerationWeight(TileSkin.CORRUPTED_DRYUAD_VILE_SIPHON, 4),
                GenerationWeight(TileSkin.CORRUPTED_DRYAD_SHADOWED_ANNIHILATION, 2)
            )
        )
    )

    val MAX_STACKS = mapOf<TileSkin, Int>(
        TileSkin.VALERIAN_RADIANT_STRIKE to 3,
        TileSkin.VALERIAN_LUMINOUS_BEAM to 4,
        TileSkin.VALERIAN_SIGIL_OF_RENEWAL to 3,
        TileSkin.THORNSTALKER_PRIMAL_ASSAULT to 3,
        TileSkin.THORNSTALKER_VENOMOUS_BARRAGE to 3,
        TileSkin.THORNSTALKER_RESILIENT_GROWTH to 2,
        TileSkin.CORRUPTED_DRYAD_ARBOREAL_FANGS to 3,
        TileSkin.CORRUPTED_DRYUAD_VILE_SIPHON to 3,
        TileSkin.CORRUPTED_DRYAD_SHADOWED_ANNIHILATION to 2,
    )
}
