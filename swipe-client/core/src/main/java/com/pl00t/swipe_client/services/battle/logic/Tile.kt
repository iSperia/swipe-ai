package com.pl00t.swipe_client.services.battle.logic

enum class TileSkin {
    VALERIAN_RADIANT_STRIKE,
    VALERIAN_LUMINOUS_BEAM,
    VALERIAN_SIGIL_OF_RENEWAL,
    VALERIAN_DIVINE_CONVERGENCE,

    THORNSTALKER_PRIMAL_ASSAULT,
    THORNSTALKER_VENOMOUS_BARRAGE,
    THORNSTALKER_RESILIENT_GROWTH,

    CORRUPTED_DRYAD_ARBOREAL_FANGS,
    CORRUPTED_DRYUAD_VILE_SIPHON,
    CORRUPTED_DRYAD_SHADOWED_ANNIHILATION,
}

data class Tile(
    val skin: TileSkin,
    val progress: Int,
    val maxProgress: Int,
    val x: Int,
    val y: Int,
    val id: Int,

)
