package com.game7th.swipe.battle.processor

import com.game7th.swipe.battle.TileSkin
import kotlin.random.Random

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

        TileSkin.THORNED_CRAWLER_VICIOUS_PINCERS to 4,
        TileSkin.THORNED_CRAWLER_DEBILIATING_STRIKE to 4,
        TileSkin.THORNED_CRAWLER_LEECHING_SHADOWS to 3,

        TileSkin.THALENDROS_THORN_WHIP to 3,
        TileSkin.THALENDROS_DARK_AURA to 5,
        TileSkin.THALENDROS_EARTHQUAKE_SLAM to 5,
    )
}
