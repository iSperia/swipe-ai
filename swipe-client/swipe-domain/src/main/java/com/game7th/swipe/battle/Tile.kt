package com.game7th.swipe.battle

import com.game7th.swipe.game.SbDisplayTileType

enum class TileSkin {
    COMMON_POISON,
    COMMON_WEAKNESS,

    VALERIAN_RADIANT_STRIKE,
    VALERIAN_LUMINOUS_BEAM,
    VALERIAN_SIGIL_OF_RENEWAL,
    VALERIAN_SIGIL_OF_RENEWAL_BG,
    VALERIAN_DIVINE_CONVERGENCE,

    THORNSTALKER_PRIMAL_ASSAULT,
    THORNSTALKER_VENOMOUS_BARRAGE,
    THORNSTALKER_RESILIENT_GROWTH,

    CORRUPTED_DRYAD_ARBOREAL_FANGS,
    CORRUPTED_DRYAD_VILE_SIPHON,
    CORRUPTED_DRYAD_SHADOWED_ANNIHILATION,

    THORNED_CRAWLER_VICIOUS_PINCERS,
    THORNED_CRAWLER_DEBILIATING_STRIKE,
    THORNED_CRAWLER_LEECHING_SHADOWS,

    THALENDROS_THORN_WHIP,
    THALENDROS_EARTHQUAKE_SLAM,
    THALENDROS_DARK_AURA,
    THALENDROS_DARK_TILE,
    THALENDROS_CORRUPTED_ROOTS
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
    val type: SbDisplayTileType,
    val meta: Any? = null,
) {
    override fun toString(): String {
        return "$skin $x:$y"
    }
}

val EMPTY_TILE = Tile(
    TileSkin.CORRUPTED_DRYAD_VILE_SIPHON,
    0,
    0,
    -10,
    -10,
    -1,
    -1,
    0,
    SbDisplayTileType.TAROT,
    null
)
