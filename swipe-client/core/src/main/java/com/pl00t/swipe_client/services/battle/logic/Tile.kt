package com.pl00t.swipe_client.services.battle.logic

enum class TileSkin {
    VALERIAN_RADIANT_STRIKE,
    VALERIAN_LUMINOUS_BEAM,
    VALERIAN_SIGIL_OF_RENEWAL,
    VALERIAN_DIVINE_CONVERGENCE,

}

data class Tile(
    val skin: String,
    val progress: Int,
    val maxProgress: Int,
    val x: Int,
    val y: Int,
    val id: Int,
    val type: TileSkin
)
