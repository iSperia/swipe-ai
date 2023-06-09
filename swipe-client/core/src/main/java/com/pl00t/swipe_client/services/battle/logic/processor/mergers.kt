package com.pl00t.swipe_client.services.battle.logic.processor

import com.pl00t.swipe_client.services.battle.logic.TileSkin

object TileMerger {
    val SIMPLE = listOf(
        TileSkin.VALERIAN_SIGIL_OF_RENEWAL,
        TileSkin.VALERIAN_RADIANT_STRIKE,
        TileSkin.VALERIAN_LUMINOUS_BEAM,
        TileSkin.CORRUPTED_DRYAD_VILE_SIPHON,
        TileSkin.CORRUPTED_DRYAD_ARBOREAL_FANGS,
        TileSkin.CORRUPTED_DRYAD_SHADOWED_ANNIHILATION,
        TileSkin.THORNSTALKER_RESILIENT_GROWTH,
        TileSkin.THORNSTALKER_VENOMOUS_BARRAGE,
        TileSkin.THORNSTALKER_PRIMAL_ASSAULT,
        TileSkin.COMMON_POISON,
        TileSkin.THORNED_CRAWLER_LEECHING_SHADOWS,
        TileSkin.THORNED_CRAWLER_DEBILIATING_STRIKE,
        TileSkin.THORNED_CRAWLER_VICIOUS_PINCERS,
    )

    val NEUTRAL = listOf<TileSkin>()
}
