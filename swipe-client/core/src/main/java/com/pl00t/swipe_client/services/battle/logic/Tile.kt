package com.pl00t.swipe_client.services.battle.logic

enum class TileSkin {
    VALERIAN_RADIANT_STRIKE,
    VALERIAN_LUMINOUS_BEAM,
    VALERIAN_SIGIL_OF_RENEWAL,
    VALERIAN_SIGIL_OF_RENEWAL_BG,
    VALERIAN_DIVINE_CONVERGENCE,

    THORNSTALKER_PRIMAL_ASSAULT,
    THORNSTALKER_VENOMOUS_BARRAGE,
    THORNSTALKER_RESILIENT_GROWTH,

    CORRUPTED_DRYAD_ARBOREAL_FANGS,
    CORRUPTED_DRYUAD_VILE_SIPHON,
    CORRUPTED_DRYAD_SHADOWED_ANNIHILATION,
}

enum class TileType {
    TAROT, BACKGROUND
}

data class Tile(
    val skin: TileSkin,
    val progress: Int,
    val maxProgress: Int,
    val x: Int,
    val y: Int,
    val id: Int,
    val layer: Int,
    val mobility: Int,
    val type: TileType,
) {
    override fun toString(): String {
        return "$skin $x:$y"
    }
}
